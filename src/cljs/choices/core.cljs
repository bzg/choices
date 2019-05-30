(ns choices.core
  (:require [reagent.core :as reagent]
            [reagent.session :as session]
            [bidi.bidi :as bidi]
            [choices.view :as view]
            [choices.input :as input]
            [accountant.core :as accountant]
            [cljsjs.clipboard]
            [clojure.string :as string]))

;; Initialize atoms and variables
(def show-help (reagent/atom view/display-help))
(def summary-answers (reagent/atom []))
(def summary-questions (reagent/atom []))
(def show-modal (reagent/atom false))
(def modal-message (reagent/atom ""))
(def bigger {:font-size "2em" :text-decoration "none"})
(def summary-display-answers (reagent/atom true))

;; Utility function to reset history
(defn reset-history []
  (reset! summary-answers [])
  (reset! summary-questions [])
  (session/put! :history []))

;; Create routes
(def app-routes
  ["/" (into
        []
        (concat [["" (keyword (:name (first input/choices)))]]
                (into [] (for [n input/choices]
                           [(:name n) (keyword (:name n))]))
                [[true :four-o-four]]))])

;; Define multimethod for later use in `create-page-contents`
(defmulti page-contents identity)

;; Create a copy-to-clipboard component
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
        [:a {:title                 (:copy-to-clipboard view/ui-strings)
             :class                 "button is-text"
             :style                 bigger
             :data-clipboard-target target}
         label])})))

;; Create all the pages
(defn create-page-contents [{:keys [done name text help no-summary
                                    force-help choices]}]
  (defmethod page-contents (keyword name) []
    [:body
     (when (not-empty view/header)
       [:section {:class (str "hero " (:color view/header))}
        [:div {:class "hero-body"}
         [:div {:class "container"}
          [:div {:class "level"}
           (if (not-empty (:logo view/header))
             [:figure {:class "media-left"}
              [:p {:class "image is-128x128"}
               [:a {:href input/home-page}
                [:img {:src (:logo view/header)}]]]])
           [:h1 {:class "title"} [:a {:href input/home-page}
                                  (:title view/header)]]
           [:h2 {:class "subtitle"} (:subtitle view/header)]]]]])
     [:div {:class "container"}
      [:div {:class (str "modal " (when @show-modal "is-active"))}
       [:div {:class "modal-background"}]
       [:div {:class "modal-content"}
        [:div {:class "box"}
         [:div {:class "title"} (:attention view/ui-strings)]
         [:p @modal-message]
         [:br]
         [:div {:class "has-text-centered"}
          [:a {:class    "button is-medium is-warning"
               :on-click #(reset! show-modal false)}
           (:ok view/ui-strings)]]]]
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
               :title    (:display-help view/ui-strings)
               :on-click #(swap! show-help not)}
           "💬"]
          ;; Done: display the copy-to-clipboard button
          [:div
           [:a {:class    "button is-text" :style bigger
                :title    (:toggle-summary-style view/ui-strings)
                :on-click #(swap! summary-display-answers not)} "🔗"]
           [clipboard-button "📋" "#copy-this"]])]
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
                      :on-click #(do (when-not no-summary
                                       (swap! summary-questions conj [text answer]))
                                     (when summary
                                       (swap! summary-answers conj summary)
                                       (when (vector? summary)
                                         (reset! show-modal true)
                                         (reset! modal-message (peek summary)))))}
                  [:div {:class (str "tile is-child box notification " color)}
                   answer]]
                 (if (and explain @show-help)
                   [:div {:class (str "tile is-child box")}
                    [:div {:class "subtitle"} explain]])])))]]
         ;; Done: display the final summary-answers
         [:div
          [:div {:id "copy-this" :class "tile is-ancestor"}
           [:div {:class "tile is-parent is-vertical is-12"}
            (for [o (if @summary-display-answers @summary-answers @summary-questions)]
              ^{:key o}
              [:div {:class "tile is-child notification"}
               (if (string? o)
                 [:div {:class "subtitle"} o]
                 [:div {:class "tile is-parent is-horizontal notification"}
                  (for [n (butlast o)]
                    ^{:key n}
                    [:div {:class "tile is-child subtitle"} n])
                  [:div {:class "tile is-child subtitle has-text-weight-bold is-size-4"}
                   (peek o)]])])]]
          [:div {:class "level-right"}
           [:a {:class    "button level-item"
                :style    bigger
                :title    (:redo view/ui-strings)
                :on-click reset-history
                :href     input/start-page} "🔃"]
           (if (not-empty view/mail-to)
             [:a {:class "button level-item"
                  :style bigger
                  :title (:mail-to-message view/ui-strings)
                  :href  (str "mailto:" view/mail-to
                              "?subject=" (:mail-subject view/ui-strings)
                              "&body=" (string/join "\n\n" @summary-answers))}
              "📩"])]])]]
     (when (not-empty view/footer)
       [:section {:class "footer"}
        [:div {:class "content has-text-centered"}
         [:p (:text view/footer)]
         [:p (:contact-intro view/ui-strings)
          [:a {:href (str "mailto:" (:contact view/footer))}
           (:contact view/footer)]]]])]))

;; Create a 404 page
(defmethod page-contents :four-o-four []
  [:body
   (when (not-empty view/header)
     [:section {:class (str "hero " (:color view/header))}
      [:div {:class "hero-body"}
       [:div {:class "container"}
        [:div {:class "level"}
         [:h1 {:class "title"} [:a {:href input/home-page}
                                (:title view/header)]]
         [:h2 {:class "subtitle"} (:subtitle view/header)]]]]])
   [:div {:class "container"}
    [:div {:class "section"}
     [:div {:class "level"}
      [:div [:h1 {:class "title"} (:404-title view/ui-strings)]
       [:h2 {:class "subtitle"} (:404-subtitle view/ui-strings)]]]
     [:a {:class "button is-info"
          :href  input/start-page}
      (:redo view/ui-strings)]]]
   (when (not-empty view/footer)
     [:section {:class "footer"}
      [:div {:class "content has-text-centered"}
       [:p (:text view/footer)]
       [:p (:contact view/footer)]]])])

;; Create all the pages from `input/choices`
(doall (map create-page-contents input/choices))

;; Create component to mount the current page
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
            current-page (:handler match)
            history      (session/get :history)]
        (cond
          (or (= current-page (keyword input/home-page))
              (= current-page (keyword input/start-page))
              (some #(= current-page %) history))
          ;; We need to reset all history information
          (reset-history)
          ;; We need to roll back history by one step
          (= current-page (peek history))
          (do (swap! summary-answers #(into [] (butlast %)))
              (swap! summary-questions #(into [] (butlast %)))
              (session/put! :history (into [] (butlast history)))))
        (session/put! :history (conj (into [] history)
                                     (session/get :current-page)))
        (session/put! :current-page current-page)))
    :path-exists?
    (fn [path] (boolean (bidi/match-route app-routes path)))})
  (accountant/dispatch-current!)
  (mount-root))
