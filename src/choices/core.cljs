(ns choices.core
  (:require [reagent.core :as reagent]
            [reagent.session :as session]
            [bidi.bidi :as bidi]
            [choices.config :as config]
            [accountant.core :as accountant]))

;; Initialize atoms
(def show-help (reagent/atom config/help))
(def output (reagent/atom {}))

;; Create bidi routes
(defn make-routes-from-input [i]
  (conj
   {"" (keyword (:name (first i)))}
   (into {} (map (fn [{:keys [name]}] {name (keyword name)}) i))
   {true :four-o-four}))

(def app-routes ["/" (make-routes-from-input config/input)])

;; Define multimethod for later use in `create-page-contents`
(defmulti page-contents identity)

(defn clean-up-summaries
  "Remove summaries associated with other choices that are have not been
  selected."
  [choices-goto goto name summary]
  (let [choices-to-remove (filter #(not= goto %) choices-goto)]
    (swap! output #(apply dissoc % choices-to-remove))
    (swap! output assoc name summary)))

;; Create all the pages
(defn create-page-contents [{:keys [start done name text help choices]}]
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
        [:div [:h1 {:class "title"} text]
         (if @show-help [:h2 {:class "subtitle"} help])]
        (if-not done
          [:a {:class    "button is-warning"
               :on-click #(swap! show-help not)} "?"])]
       (if done
         [:div
          [:div {:class "tile is-ancestor"}
           [:div {:class "tile is-parent is-vertical is-12"}
            (for [o (vals @output)]
              ^{:key o}
              [:div {:class "tile is-child notification"}
               [:div {:class "subtitle"} o]])]]
          [:a {:class "button is-success"
               :href  "/"} "▶"]]
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
                                   (if start ;; FIXME: temporary fix
                                     (reset! output {name summary})
                                     (swap! output conj
                                            (clean-up-summaries
                                             choices-goto goto name summary))))}
                   answer]]
                 (if (and explain @show-help)
                   [:div {:class (str "tile is-child box")}
                    [:div {:class "subtitle"} explain]])])))]])]]
     (when (not-empty config/footer)
       [:section {:class "footer"}
        [:div {:class "content has-text-centered"}
         [:p (:text config/footer)]
         [:p (:contact config/footer)]]])]))

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
