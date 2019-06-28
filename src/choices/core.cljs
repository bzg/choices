;; Copyright (c) 2019 DINSIC, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSES/EPL-2.0.txt

(ns choices.core
  (:require [reagent.core :as reagent]
            [reagent.format :as fmt]
            [reagent.session :as session]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [choices.i18n :as i18n]
            [choices.config :as config]
            [cljsjs.clipboard]
            [clojure.string :as string]
            [markdown-to-hiccup.core :as md]
            [taoensso.tempura :refer [tr]]))

;; UI variables
(def bigger {:font-size "2em" :text-decoration "none"})

;; Modal variables
(def show-help (reagent/atom config/display-help))
(def show-modal (reagent/atom false))
(def show-summary-answers (reagent/atom true))
(def modal-message (reagent/atom ""))

;; home-page and start-page
(def home-page
  (first (remove nil? (map #(when (:home-page %) (keyword (:name %)))
                           config/tree))))
(def start-page
  (first (remove nil? (map #(when (:start-page %) (keyword (:name %)))
                           config/tree))))

(defn md-to-string [s]
  (-> s (md/md->hiccup) (md/component)))

;; History-handling variables
(def history (reagent/atom [{:score config/score}]))
(def hist-to-redo (reagent/atom {}))
(def hist-to-add (reagent/atom {}))

;; Localization variables
(def localization-custom
  (into {} (map (fn [locale] {(key locale)
                              (merge (val locale) config/ui-strings)})
                i18n/localization)))

(def lang (keyword (or (not-empty config/locale) "en-GB")))
(def opts {:dict localization-custom})
(def i18n (partial tr opts [lang]))

;; Create routes
(def routes
  (into [] (for [n config/tree] [(:name n) (keyword (:name n))])))

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
     (when (not-empty config/header)
       [:section {:class (str "hero " (:color config/header))}
        [:div {:class "hero-body"}
         [:div {:class "container"}
          [:div {:class "level"}
           (if (not-empty (:logo config/header))
             [:figure {:class "media-left"}
              [:p {:class "image is-128x128"}
               [:a {:href (rfe/href home-page)}
                [:img {:src (:logo config/header)}]]]])
           [:h1 {:class "title"} (:title config/header)]
           [:h2 {:class "subtitle"}
            (md-to-string (:subtitle config/header))]]]]])
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
         [:h1 {:class "title"} (md-to-string text)]
         (when (or force-help @show-help)
           [:div {:style {:margin "1em"}} (md-to-string help)])]
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
                :on-click #(swap! show-summary-answers not)} "🔗"]
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
                      :on-click #(do (when (vector? summary)
                                       (reset! show-modal true)
                                       (reset! modal-message (peek summary)))
                                     (reset! hist-to-add
                                             (merge
                                              {:score (merge-with + (:score (peek @history)) score)}
                                              {:questions (when-not no-summary [text answer])}
                                              {:answers summary})))}
                  [:div {:class (str "tile is-child box notification " color)}
                   (md-to-string answer)]]
                 (if (and explain @show-help)
                   [:div {:class (str "tile is-child box")}
                    [:div {:class "subtitle"}
                     (md-to-string explain)]])])))]]
         ;; Done: display the final summary-answers
         [:div
          [:div {:id "copy-this" :class "tile is-ancestor"}
           [:div {:class "tile is-parent is-vertical is-12"}
            ;; Display score
            (if (not-empty (:score (peek @history)))
              [:div {:class "tile is-parent is-horizontal is-12"}
               (for [s (:score (peek @history))]
                 ^{:key (pr-str s)}
                 [:div {:class "tile is-child box"}
                  (str (first s) ": " (second s))])])
            ;; Display answers
            (for [o (if @show-summary-answers
                      (reverse (:answers (peek @history)))
                      (reverse (:questions (peek @history))))]
              ^{:key o}
              [:div {:class "tile is-child notification"}
               (if (string? o)
                 [:div {:class "subtitle"} (md-to-string o)]
                 [:div {:class "tile is-parent is-horizontal notification"}
                  (for [n (butlast o)]
                    ^{:key n}
                    [:div {:class "tile is-child subtitle"} (md-to-string n)])
                  [:div {:class "tile is-child subtitle has-text-centered has-text-weight-bold is-size-4"}
                   (peek (md-to-string o))]])])]]
          [:div {:class "level-right"}
           [:a {:class "button level-item"
                :style bigger
                :title (i18n [:redo])
                :href  (rfe/href start-page)} "🔃"]
           (if (not-empty config/mail-to)
             [:a {:class "button level-item"
                  :style bigger
                  :title (i18n [:mail-to-message])
                  :href  (str "mailto:" config/mail-to
                              "?subject=" (i18n [:mail-subject])
                              "&body="
                              (string/replace
                               (fmt/format (i18n [:mail-body])
                                           (string/join "%0D%0A%0D%0A"
                                                        (flatten (:answers (peek @history)))))
                               #"[\n\t]" "%0D%0A%0D%0A"))}
              "📩"])]])]]
     (when (not-empty config/footer)
       [:section {:class "footer"}
        [:div {:class "content has-text-centered"}
         [:p (md-to-string (:text config/footer))]
         (when-let [c (not-empty (:contact config/footer))]
           [:p (i18n [:contact-intro])
            [:a {:href (str "mailto:" (:contact config/footer))}
             (:contact config/footer)]])]])]))

;; Create all the pages from `config/tree`
(doall (map create-page-contents config/tree))

;; Create component to mount the current page
(defn current-page []
  (let [page (or (session/get :current-page) home-page)]
    [:div ^{:key page} [page-contents page]]))

(defn on-navigate [match]
  (let [target-page (:name (:data match))
        tmp-hist    @history
        prev        (peek tmp-hist)]
    (cond
      ;; Reset history?
      (= target-page start-page)
      (do (reset! history [{:score config/score}])
          (reset! hist-to-add {}))
      ;; History backward?
      (= target-page (first (:page (peek tmp-hist))))
      (reset! history (into [] (butlast tmp-hist)))
      ;; History forward?
      (= target-page (first (:page @hist-to-redo)))
      (swap! history conj @hist-to-redo)
      :else
      (swap! history
             conj {:page      (conj (:page prev) (session/get :current-page))
                   :questions (conj (:questions prev) (:questions @hist-to-add))
                   :answers   (conj (:answers prev) (:answers @hist-to-add))
                   :score     (conj (:score prev) (:score @hist-to-add))}))
    (reset! hist-to-redo (peek tmp-hist))
    (session/put! :current-page target-page)))

(defn ^:export init []
  (rfe/start!
   (rf/router routes)
   on-navigate
   {:use-fragment true})
  (reagent/render-component
   [current-page]
   (. js/document (getElementById "app"))))
