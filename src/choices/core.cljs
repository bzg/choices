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
(def output (reagent/atom {}))

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

(defn clean-up-summaries
  "Remove summaries associated with other choices that are have not been
  selected."
  [choices-goto goto name summary]
  (let [choices-to-remove (filter #(not= goto %) choices-goto)]
    (swap! output #(apply dissoc % choices-to-remove))
    (swap! output assoc name summary)))

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
         (.destroy @clipboard-atom)
         (reset! clipboard-atom nil))
      :reagent-render
      (fn []
        [:a {:title                 (:fr (:copy-to-clipboard config/i18n))
             :class                 "button is-text"
             :style                 bigger
             :data-clipboard-target target}
         label])})))

;; Create all the pages
(defn create-page-contents [{:keys [start done name text help force-help choices]}]
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
               [:img {:src (:logo config/header)}]]])
           [:h1 {:class "title"} [:a {:href "/"} (:title config/header)]]
           [:h2 {:class "subtitle"} (:subtitle config/header)]]]]])
     [:div {:class "container"}
      [:div {:class "section"}
       [:div {:class "level"}
        [:div
         [:h1 {:class "title"} text]
         (when (or force-help @show-help)
           [:div {:style {:margin "1em" :font-size "1.2rem"}} help])]
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
                 [:div {:class (str "tile is-child box notification " color)}
                  [:a {:class    "title"
                       :style    {:text-decoration "none"}
                       :href     (bidi/path-for app-routes (keyword goto))
                       :on-click (fn []
                                   (when (not-empty summary)
                                     (if start ;; FIXME: temporary fix
                                       (reset! output {name summary})
                                       (clean-up-summaries
                                        choices-goto goto name summary))))}
                   answer]]
                 (if (and explain @show-help)
                   [:div {:class (str "tile is-child box")}
                    [:div {:class "subtitle"} explain]])])))]]
         ;; Done: display the final output
         [:div
          [:div {:id "copy-this" :class "tile is-ancestor"}
           [:div {:class "tile is-parent is-vertical is-12"}
            (for [o (vals @output)]
              ^{:key o}
              [:div {:class "tile is-child notification"}
               [:div {:class "subtitle"} o]])]]
          [:div {:class "level-right"}
           [:a {:class    "button level-item"
                :style    bigger
                :title    (:fr (:redo config/i18n))
                :on-click #(reset! output nil)
                :href     "/"} "🔃"]
           (if (not-empty config/mail-to)
             [:a {:class "button level-item"
                  :style bigger
                  :title (:fr (:mail-to-message config/i18n))
                  :href  (str "mailto:" config/mail-to
                              "?subject=" (:fr (:mail-subject config/i18n))
                              "&body=" (string/join "\n" (vals @output)))}
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
         [:h1 {:class "title"} [:a {:href "/"} (:title config/header)]]
         [:h2 {:class "subtitle"} (:subtitle config/header)]]]]])
   [:div {:class "container"}
    [:div {:class "section"}
     [:div {:class "level"}
      [:div [:h1 {:class "title"} "404 - wrong page?"]
       [:h2 {:class "subtitle"} "Can we help?"]]]
     [:a {:href "/" :class "button is-info"} "Start over"]]]
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
    (fn [path] (let [match        (bidi/match-route app-routes path)
                     current-page (:handler match)]
                 (session/put! :current-page current-page)))
    :path-exists?
    (fn [path] (boolean (bidi/match-route app-routes path)))})
  (accountant/dispatch-current!)
  (mount-root))
