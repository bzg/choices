(ns choices.core
  (:require [reagent.core :as reagent]
            [reagent.session :as session]
            [bidi.bidi :as bidi]
            [choices.config :as config]
            [accountant.core :as accountant]
            [cljsjs.clipboard]
            [clojure.string :as string]))

;; Initialize atoms
(def show-help (reagent/atom config/display-help))
(def output (reagent/atom []))
(def show-modal (reagent/atom false))
(def modal-message (reagent/atom ""))

;; Utility
(def bigger {:font-size "2em" :text-decoration "none"})

;; Create bidi routes
(def app-routes
  ["/" (into []
             (concat [["" (keyword (:name (first config/input)))]]
                     (into [] (for [n config/input]
                                [(:name n) (keyword (:name n))]))
                     [[true :four-o-four]]))])

;; Define multimethod for later use in `create-page-contents`
(defmulti page-contents identity)

;; Create copy-to-clipboard component
(defn clipboard-button [label target]
  (let [clipboard-atom (reagent/atom nil)]
    (reagent/create-class
     {:display-name "clipboard-button"
      :component-did-mount
      #(let [clipboard (new js/ClipboardJS (reagent/dom-node %))]
         (reset! clipboard-atom clipboard))
      :component-will-unmount
      #(when-not (nil? @clipboard-atom)
         (reset! clipboard-atom nil))
      :reagent-render
      (fn []
        [:a {:title                 (:fr (:copy-to-clipboard config/i18n))
             :class                 "button is-text"
             :style                 bigger
             :data-clipboard-target target}
         label])})))

;; Create all the pages
(defn create-page-contents [{:keys [done name text help force-help choices]}]
  (defmethod page-contents (keyword name) []
    [:body
     (when (not-empty config/header)
       [:section {:class (str "hero " (:color config/header))}
        [:div {:class "hero-body"}
         [:div {:class "container"}
          [:div {:class "level"}
           (if (not-empty (:logo config/header))
             [:figure {:class "media-left"}
              [:p {:class "image is-128x128"}
               [:a {:href config/default-page}
                [:img {:src (:logo config/header)}]]]])
           [:h1 {:class "title"} [:a {:href config/default-page}
                                  (:title config/header)]]
           [:h2 {:class "subtitle"} (:subtitle config/header)]]]]])
     [:div {:class "container"}
      [:div {:class (str "modal " (when @show-modal "is-active"))}
       [:div {:class "modal-background"}]
       [:div {:class "modal-content"}
        [:div {:class "box"}
         [:div {:class "title"} (:fr (:attention config/i18n))]
         [:p @modal-message]
         [:br]
         [:div {:class "has-text-centered"}
          [:a {:class    "button is-medium is-warning"
               :on-click #(reset! show-modal false)}
           (:fr (:ok config/i18n))]]]]
       [:button {:class    "modal-close is-large" :aria-label "close"
                 :on-click #(reset! show-modal false)}]]
      [:div {:class "section"}
       [:div {:class "level"}
        [:div
         [:h1 {:class "title"} text]
         (when (or force-help @show-help)
           [:div {:style {:margin "1em"}} help])]
        (if-not done
          ;; Not done: display the help button
          [:a {:class    "button is-text"
               :style    bigger
               :title    (:fr (:display-help config/i18n))
               :on-click #(swap! show-help not)} "💬"]
          ;; Done: display the copy-to-clipboard button
          [clipboard-button "📋" "#copy-this"])]
       (if-not done
         ;; Not done: display the choices
         [:div {:class "tile is-ancestor"}
          [:div {:class "tile is-parent"}
           (let [choices-goto (map :goto choices)]
             (doall
              (for [{:keys [answer goto explain color summary] :as c} choices]
                ^{:key c}
                [:div {:class "tile is-parent is-vertical"}
                 [:a {:class    "title"
                      :style    {:text-decoration "none"}
                      :href     (bidi/path-for app-routes (keyword goto))
                      :on-click #(when summary
                                   (swap! output conj summary)
                                   (when (vector? summary)
                                     (reset! show-modal true)
                                     (reset! modal-message (peek summary))))}
                  [:div {:class (str "tile is-child box notification " color)}
                   answer]]
                 (if (and explain @show-help)
                   [:div {:class (str "tile is-child box")}
                    [:div {:class "subtitle"} explain]])])))]]
         ;; Done: display the final output
         [:div
          [:div {:id "copy-this" :class "tile is-ancestor"}
           [:div {:class "tile is-parent is-vertical is-12"}
            (for [o @output]
              ^{:key o}
              [:div {:class "tile is-child notification"}
               (if (string? o)
                 [:div {:class "subtitle"} o]
                 [:div {:class "tile is-parent is-horizontal notification"}
                  (for [n (butlast o)]
                    ^{:key n}
                    [:div {:class "tile is-child subtitle"} n])
                  [:div {:class "tile is-child subtitle has-text-weight-bold"}
                   (peek o)]])])]]
          [:div {:class "level-right"}
           [:a {:class    "button level-item"
                :style    bigger
                :title    (:fr (:redo config/i18n))
                :on-click #(reset! output [])
                :href     config/start-page} "🔃"]
           (if (not-empty config/mail-to)
             [:a {:class "button level-item"
                  :style bigger
                  :title (:fr (:mail-to-message config/i18n))
                  :href  (str "mailto:" config/mail-to
                              "?subject=" (:fr (:mail-subject config/i18n))
                              "&body=" (string/join "\n\n" @output))}
              "📩"])]])]]
     (when (not-empty config/footer)
       [:section {:class "footer"}
        [:div {:class "content has-text-centered"}
         [:p (:text config/footer)]
         [:p "Contact: "
          [:a {:href (str "mailto:" (:contact config/footer))}
           (:contact config/footer)]]]])]))

(defmethod page-contents :four-o-four []
  [:body
   (when (not-empty config/header)
     [:section {:class (str "hero " (:color config/header))}
      [:div {:class "hero-body"}
       [:div {:class "container"}
        [:div {:class "level"}
         [:h1 {:class "title"} [:a {:href config/default-page}
                                (:title config/header)]]
         [:h2 {:class "subtitle"} (:subtitle config/header)]]]]])
   [:div {:class "container"}
    [:div {:class "section"}
     [:div {:class "level"}
      [:div [:h1 {:class "title"} (:fr (:404-title config/i18n))]
       [:h2 {:class "subtitle"} (:fr (:404-subtitle config/i18n))]]]
     [:a {:class "button is-info"
          :href  config/start-page}
      (:fr (:redo config/i18n))]]]
   (when (not-empty config/footer)
     [:section {:class "footer"}
      [:div {:class "content has-text-centered"}
       [:p (:text config/footer)]
       [:p (:contact config/footer)]]])])

;; Main function: create all the pages from `config/input`
(doall (map create-page-contents config/input))

;; Page mounting component
(defn current-page []
  (let [page (session/get :current-page)]
    [:div
     ^{:key page} [page-contents page]]))

(defn mount-root []
  (reagent/render-component
   [current-page]
   (. js/document (getElementById "app"))))

(defn ^:export init []
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (let [match        (bidi/match-route app-routes path)
            current-page (:handler match)]
        (cond
          (= current-page (keyword config/default-page))
          (reset! output [])
          (= current-page (peek (session/get :history)))
          (do (swap! output #(into [] (butlast %)))
              (session/put! :history (into [] (butlast (session/get :history))))))
        (session/put! :history (conj (into [] (session/get :history))
                                     (session/get :current-page)))
        (session/put! :current-page current-page)))
    :path-exists?
    (fn [path] (boolean (bidi/match-route app-routes path)))})
  (accountant/dispatch-current!)
  (mount-root))

