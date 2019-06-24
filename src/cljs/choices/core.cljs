;; Copyright (c) 2019 DINSIC, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSES/EPL-2.0.txt

(ns choices.core
  (:require [reagent.core :as reagent]
            [reagent.session :as session]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [choices.view :as view]
            [choices.input :as input]
            [cljsjs.clipboard]
            [clojure.string :as string]
            [taoensso.tempura :refer [tr]]))

;; Initialize atoms and variables
(def show-help (reagent/atom view/display-help))
(def summary-answers (reagent/atom []))
(def summary-questions (reagent/atom []))
(def show-modal (reagent/atom false))
(def modal-message (reagent/atom ""))
(def bigger {:font-size "2em" :text-decoration "none"})
(def summary-display-answers (reagent/atom true))
(def final-score (reagent/atom input/score))
(def last-score-change (reagent/atom {}))
(def history (reagent/atom []))
(def home-page
  (first (remove nil? (map #(when (:home-page %)
                              (keyword (:name %)))
                           input/choices))))
(def start-page
  (first (remove nil? (map #(when (:start-page %)
                              (keyword (:name %)))
                           input/choices))))

(def localization
  {:en-GB
   {:display-help         "Display help"
    :copy-to-clipboard    "Copy in the clipboard"
    :mail-to-message      "Send by email"
    :mail-subject         "Results"
    :redo                 "Redo"
    :ok                   "Okay"
    :contact-intro        "Contact: "
    :toggle-summary-style "Toggle summary style"
    :attention            "Attention"}
   :fr-FR
   {:display-help         "Afficher de l'aide"
    :copy-to-clipboard    "Copier dans le presse-papier"
    :mail-to-message      "Envoyer par mail"
    :mail-subject         "Résultats"
    :redo                 "Recommencer"
    :ok                   "D'accord"
    :contact-intro        "Contact : "
    :toggle-summary-style "Changer le style de résumé"
    :attention            "Attention"}})

(def localization-custom
  (into {}
        (map (fn [locale] {(key locale)
                           (merge (val locale) view/ui-strings)})
             localization)))

(def lang (keyword (or (not-empty view/locale) "en-GB")))
(def opts {:dict localization-custom})
(def i18n (partial tr opts [lang]))

;; Utility function to reset state
(defn reset-state []
  (reset! summary-answers [])
  (reset! summary-questions [])
  (reset! final-score input/score)
  (reset! history []))

;; Create routes
(def app-routes
  (into [] (for [n input/choices] [(:name n) (keyword (:name n))])))

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
        [:a {:title                 (i18n [:copy-to-clipboard])
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
               [:a {:href (rfe/href home-page)}
                [:img {:src (:logo view/header)}]]]])
           [:h1 {:class "title"} (:title view/header)]
           [:h2 {:class "subtitle"} (:subtitle view/header)]]]]])
     [:div {:class "container"}
      [:div {:class (str "modal " (when @show-modal "is-active"))}
       [:div {:class "modal-background"}]
       [:div {:class "modal-content"}
        [:div {:class "box"}
         [:div {:class "title"} (i18n [:attention])]
         [:p @modal-message]
         [:br]
         [:div {:class "has-text-centered"}
          [:a {:class    "button is-medium is-warning"
               :on-click #(reset! show-modal false)}
           (i18n [:ok])]]]]
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
               :title    (i18n [:display-help])
               :on-click #(swap! show-help not)}
           "💬"]
          ;; Done: display the copy-to-clipboard button
          [:div
           [:a {:class    "button is-text" :style bigger
                :title    (i18n [:toggle-summary-style])
                :on-click #(swap! summary-display-answers not)} "🔗"]
           [clipboard-button "📋" "#copy-this"]])]
       (if-not done
         ;; Not done: display the choices
         [:div {:class "tile is-ancestor"}
          [:div {:class "tile is-parent"}
           (let [choices-goto (map :goto choices)]
             (doall
              (for [{:keys [answer goto explain color summary score] :as c} choices]
                ^{:key c}
                [:div {:class "tile is-parent is-vertical"}
                 [:a {:class    "title"
                      :style    {:text-decoration "none"}
                      :href     (rfe/href (keyword goto))
                      :on-click #(do (when score
                                       (swap! final-score (fn [s] (merge-with + s score)))
                                       (reset! last-score-change score))
                                     (when-not no-summary
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
            (if (not-empty @final-score)
              [:div {:class "tile is-parent is-horizontal is-12"}
               (for [s @final-score]
                 ^{:key s}
                 [:div {:class "tile is-child box"}
                  (str (first s) ": " (second s))])])
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
                :title    (i18n [:redo])
                :on-click reset-state
                :href     (rfe/href start-page)} "🔃"]
           (if (not-empty view/mail-to)
             [:a {:class "button level-item"
                  :style bigger
                  :title (i18n [:mail-to-message])
                  :href  (str "mailto:" view/mail-to
                              "?subject=" (i18n [:mail-subject])
                              "&body=" (string/join "%0D%0A%0D%0A"
                                                    (flatten @summary-answers)))}
              "📩"])]])]]
     (when (not-empty view/footer)
       [:section {:class "footer"}
        [:div {:class "content has-text-centered"}
         [:p (:text view/footer)]
         (when-let [c (not-empty (:contact view/footer))]
           [:p (i18n [:contact-intro])
            [:a {:href (str "mailto:" (:contact view/footer))}
             (:contact view/footer)]])]])]))

;; Create all the pages from `input/choices`
(doall (map create-page-contents input/choices))

;; Create component to mount the current page
(defn current-page []
  (let [page (or (session/get :current-page) home-page)]
    [:div
     ^{:key page} [page-contents page]]))

(defn mount-root []
  (reagent/render-component
   [current-page]
   (. js/document (getElementById "app"))))

(defn ^:export init []
  (rfe/start!
   (rf/router app-routes)
   (fn [match]
     (let [target-page   (:name (:data match))
           local-history @history]
       (swap! history conj (session/get :current-page))
       (cond
         (or (= target-page home-page)
             (= target-page start-page))
         ;; We need to reset all history information
         (reset-state)
         ;; We need to roll back history by one step
         (= target-page (peek local-history))
         (do (swap! summary-answers #(into [] (butlast %)))
             (swap! summary-questions #(into [] (butlast %)))
             (swap! final-score #(merge-with - % @last-score-change))
             (reset! history (into [] (butlast local-history)))))
       (session/put! :current-page target-page)))
   {:use-fragment true})
  (mount-root))
