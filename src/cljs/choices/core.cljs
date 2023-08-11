;; Copyright (c) 2019-2023 Bastien Guerry <bzg@gnu.org>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSES/EPL-2.0.txt

(ns choices.core
  (:require [reagent.core :as reagent]
            [reagent.dom]
            [reagent.format :as fmt]
            [reagent.session :as session]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [choices.i18n :as i18n]
            [choices.macros :as macros]
            [cljsjs.clipboard]
            [cljs.reader]
            [clojure.string :as string]
            [markdown-to-hiccup.core :as md]
            [taoensso.tempura :refer [tr]]))

;; General configuration
(def config (macros/inline-yaml-resource "config.yml"))
(def theme (:theme config))
(def t (into {} (map (fn [[k v]]
                       (if (= theme "chota")
                         [k ((keyword theme) v)]
                         ;; For bulma and dsfr, keep keyword as name
                         [k (string/replace (name k) "_" " ")]))
                     (macros/inline-edn-resource "theme.edn"))))

;; Variables
(def show-summary-answers (reagent/atom true))
(def show-summary (:display-summary config))
(def conditional-score-output (:conditional-score-output config))
(def score-variables (:score-variables config))
(def tree (:tree config))

;; Home-page and start-page
(def home-page
  (first (remove nil? (map #(when (:home-page %) (keyword (:node %))) tree))))
(def start-page
  (first (remove nil? (map #(when (:start-page %) (keyword (:node %))) tree))))

(defn md-to-string [^String s]
  (-> s (md/md->hiccup) (md/component) peek))

;; History-handling variables
(def history (reagent/atom [{:score score-variables}]))
(def hist-to-redo (reagent/atom {}))
(def hist-to-add (reagent/atom {}))

;; Localization variables
(def localization-custom
  (into {} (map (fn [locale] {(key locale)
                              (merge (val locale) (:ui-strings config))})
                i18n/localization)))
(def lang (keyword (or (not-empty (:locale config)) "en")))
(def opts {:dict localization-custom})
(def i18n (partial tr opts [lang]))

;; Create a copy-to-clipboard component
(defn clipboard-button [label target]
  (let [clipboard-atom (reagent/atom nil)]
    (reagent/create-class
     {:display-name "clipboard-button"
      :component-did-mount
      #(let [clipboard (new js/ClipboardJS (reagent.dom/dom-node %))]
         (reset! clipboard-atom clipboard))
      :component-will-unmount
      #(when-not (nil? @clipboard-atom) (reset! clipboard-atom nil))
      :reagent-render
      (fn []
        [:a
         {:class                 (:button_is-outlined t)
          :title                 (i18n [:copy-to-clipboard])
          :data-clipboard-target target}
         label])})))

;; Utility functions
(defn strip-html-tags [^string s]
  (if (string? s) (string/replace s #"<([^>]+)>" "") s))

(defn sort-map-by-score-values [m]
  (into (sorted-map-by
         (fn [k1 k2] (compare [(:value (get m k2)) k2]
                              [(:value (get m k1)) k1])))
        m))

(defn all-vals-compare? [f m1 m2]
  (every? true? (for [[k v] m1] (f v (k m2)))))

;; Create routes
(def routes (into [] (for [n tree] [(:node n) (keyword (:node n))])))

;; Define multimethod for later use in `create-page-contents`
(defmulti page-contents identity)

(defn header []
  [:header {:class (:container t)}
   [:div {:class (:hero-body t)}
    [:div {:class (:has-text-centered t)}
     [:div
      [:h1 {:class (:title t)} (:title (:header config))]
      [:h2 {:class (:subtitle t)}
       (md-to-string (:subtitle (:header config)))]]]]])

(defn footer []
  [:footer {:class (:footer t)}
   [:div {:class (:container t)}
    [:br]
    [:div {:class (:has-text-centered t)}
     [:div
      (md-to-string (:text (:footer config)))
      (when-let [c (not-empty (:contact (:footer config)))]
        [:p (i18n [:contact-intro])
         [:a {:href (str "mailto:" c)} c]])]]]])

(defn score-details [scores]
  (for [row-score (partition-all 5 scores)]
    ^{:key (random-uuid)}
    [:div {:class (str (:columns t))}
     (for [s row-score]
       ^{:key (random-uuid)}
       [:div {:class (:column t)}
        (let [v (val s) value (:value v)]
          (str (:display v) ": " value))])]))

(defn score-top-result [scores]
  (let [final-scores  (sort-map-by-score-values scores)
        last-score    (first final-scores)
        butlast-score (second final-scores)]
    (when (> (:value (val last-score)) (:value (val butlast-score)))
      (when-let [s (:as-top-result-display (val last-score))]
        [:div {:class (str (:notification t) " " (:is-warning t))}
         [:p (:as-top-result-display s)]]))))

(defn conditional-score-result [scores conclusions]
  (let [conditions (atom nil)
        matching   (atom nil)
        output     (atom "")
        color      (atom "")
        node       (atom "")]
    (doseq [[_ cas] conclusions
            :let    [col (:color cas)
                     msg (:message cas)
                     nod (:node cas)
                     pri (:priority cas)
                     cds (dissoc cas :message :color :priority)]]
      (doseq [condition cds]
        (swap! conditions conj
               (merge (val condition) {:msg msg :col col :pri pri :nod nod}))))
    (doseq [c0   @conditions
            :let [c  (dissoc c0 :msg :col :pri :nod)
                  ks (keys c)]]
      (when (all-vals-compare?
             (fn [a b] (if (zero? a) (= a b) (>= a b)))
             (select-keys scores ks) c)
        (swap! matching conj c0)))
    (let [match (first (sort-by :pri @matching))]
      (reset! output (:msg match))
      (reset! color (:col match))
      (reset! node (:nod match)))
    ;; Return the expected map:
    {:color  @color
     :output @output
     :node   @node}))

(defn format-score-output-string [output scores]
  (let [scores
        (map (fn [[k v]]
               [(str "%" (name k))
                (if (:as-percent (get score-variables k))
                  (when-let [max (:max (get score-variables k))]
                    (fmt/format "%.0f" (/ (* v 100) max)))
                  v)])
             scores)]
    (reduce-kv string/replace output (into {} scores))))

(defn scores-result [scores]
  [:div
   (when (:display-score config)
     [:div
      ;; Optional, mainly for debugging purpose
      (when (:display-score-details config)
        (score-details scores))
      ;; Only when no conditional score output and
      ;; When :as-top-result-display is set for each score
      (when (and (not conditional-score-output)
                 (:display-score-top-result config))
        (score-top-result scores))
      ;; Only when score-results is defined
      (let [scores (apply merge (map (fn [[k v]] {k (:value v)}) scores))]
        (when conditional-score-output
          (let [{:keys [color output]}
                (conditional-score-result
                 scores conditional-score-output)]
            (when (not-empty output)
              [:div
               {:class (str (:notification t) " " (or ((keyword color) t) (:is-info t)))}
               (md-to-string
                (format-score-output-string output scores))]))))
      ;; Always display display-unconditionally when not empty
      (when-let [sticky (:display-unconditionally config)]
        [:div {:class (:notification t)} (md-to-string sticky)])])
   [:br]])

(defn summary []
  (for [o (if @show-summary-answers
            (remove nil? (reverse (:answers (peek @history))))
            (remove nil? (reverse (:questions (peek @history)))))]
    ^{:key (random-uuid)}
    (cond
      ;; This is a list of assertions
      (and (not-empty o) (string? o))
      [:div {:class (:notification t) :key (random-uuid)}
       [:div (md-to-string o)]]
      ;; This is a list of questions/answers
      (not-empty (butlast o))
      [:div
       {:class (:notification t) :key (random-uuid)}
       [:div
        (when-let [n (not-empty (first (butlast o)))]
          (md-to-string n))
        (when-let [a (not-empty (peek o))]
          [:strong (md-to-string a)])]])))

(defn get-target-node [goto current-score]
  (cond (string? goto)
        (keyword goto)
        (map? goto)
        (if (and (:conditional-navigation config)
                 (:conditional-score-output goto))
          (let [score  current-score
                score  (apply merge (map (fn [[k v]] {k (:value v)}) score))
                result (conditional-score-result
                        score conditional-score-output)]
            (keyword (or (:node result) (get goto :default))))
          (let [score   (apply merge (map (fn [[k v]] {k (:value v)}) current-score))
                matches (doall
                         (for [[cnd-name value-node]
                               goto
                               :let [kname (keyword cnd-name)
                                     cnd-val  (:value value-node)
                                     cnd-node (:node value-node)]]
                           (when (and (= cnd-val (get score kname))
                                      (string? cnd-node))
                             cnd-node)))]
            (keyword (or (first (remove nil? matches))
                         (get goto :default)))))))

(defn merge-scores [previous_score current_score]
  (merge-with
   (fn [a b] {:display               (:display a)
              :as-top-result-display (:as-top-result-display a)
              :value
              (let [a_v (:value a) b_v (:value b)]
                (cond (and (integer? a_v) (integer? b_v))
                      (+ (:value a) (:value b))
                      ;; One boolean and/or one string, take last:
                      :else b_v))})
   previous_score current_score))

(defn display-choices [choices text no-summary]
  (doall
   (for [choices-row (partition-all 4 choices)]
     ^{:key (random-uuid)}
     [:div {:class (str (:columns t) " " (:has-text-centered t))}
      (doall
       (for [{:keys [answer goto explain color summary score]} choices-row]
         ^{:key (random-uuid)}
         [:div {:class (:column t)}
          [:button.section
           {:class (str (:button t) " " (:is-fullwidth t) " " ((keyword color) t))
            :on-click
            #(do (when (vector? summary) (js/alert (peek summary)))
                 (let [current-score
                       (merge-scores (:score (peek @history)) score)]
                   (reset! hist-to-add
                           (merge
                            {:score current-score}
                            {:questions (when-not no-summary [text answer])}
                            {:answers summary}))
                   (rfe/push-state
                    (get-target-node goto current-score))))}
           [:div
            (md-to-string answer)
            (when explain
              (md-to-string explain))]]]))])))

;; Create all the pages
(defn create-page-contents [{:keys [done node text help no-summary
                                    progress choices]}]
  (defmethod page-contents (keyword node) []
    [:div {:class (:container t)}
     (when (not-empty (:header config)) (header))
     [:div.section
      (when-let [[v m] (cljs.reader/read-string progress)]
        [:progress
         {:class (str (:progress t) " " (:is-fullwidth t) " " (:is-info t))
          :value v :max m}])
      ;; Main question (text)
      [:h3 {:class (:subtitle t)} (md-to-string text)]
      (when done
        ;; Done: display the copy-to-clipboard button
        [:div
         [:a
          {:class    (:button_is-outlined t)
           :title    (i18n [:toggle-summary-style])
           :on-click #(swap! show-summary-answers not)} "🔗"]
         [clipboard-button "📋" "#copy-this"]
         [:a {:class (:button_is-outlined t)
              :title (i18n [:redo])
              :href  (rfe/href start-page)} "🔃"]
         (when (not-empty (:mail-to config))
           (let [contents (or (not-empty (remove nil? (:answers (peek @history))))
                              (not-empty (remove nil? (:questions (peek @history))))
                              (i18n [:mail-body-default]))
                 body     (->> contents flatten
                               (map strip-html-tags)
                               reverse
                               (string/join "%0a")
                               (fmt/format (i18n [:mail-body])))]
             [:a
              {:class (:button_is-outlined t)
               :title (i18n [:mail-to-message])
               :href  (str "mailto:" (:mail-to config)
                           "?subject=" (i18n [:mail-subject])
                           "&body="
                           (string/replace body #"[\n\t]" "%0a"))}
              "📩"]))])
      [:br]
      (when-let [help-message (not-empty help)]
        [:div {:class (:notification t)}
         (md-to-string help-message)])
      [:br]
      (if-not done
        ;; Not done: display the choices
        (display-choices choices text no-summary)
        ;; Done: display the final summary-answers
        [:div
         [:div {:id "copy-this"}
          ;; Display score
          (when-let [scores (:score (peek @history))]
            (scores-result scores))
          ;; Display answers
          (when show-summary (summary))]])]
     (when (not-empty (:footer config)) (footer))]))

;; Create all the pages from config.yml
(dorun (map create-page-contents tree))

;; Create component to mount the current page
(defn current-page []
  (let [page (or (session/get :current-page) home-page)]
    [:div ^{:key random-uuid} [page-contents page]]))

;; Setup navigation
(defn on-navigate [match]
  (let [target-page (:name (:data match))
        prev        (peek @history)]
    (cond
      ;; Reset history?
      (= target-page start-page)
      (do (reset! history [{:score score-variables}])
          (reset! hist-to-redo {})
          (reset! hist-to-add {}))
      ;; History backward?
      (= target-page (first (:page (peek @history))))
      (reset! history (into [] (butlast @history)))
      ;; History forward?
      (= target-page (first (:page @hist-to-redo)))
      (swap! history conj @hist-to-redo)
      :else
      (swap! history
             conj {:page      (conj (:page prev) (session/get :current-page))
                   :questions (conj (:questions prev) (:questions @hist-to-add))
                   :answers   (conj (:answers prev) (:answers @hist-to-add))
                   :score     (conj (:score prev) (:score @hist-to-add))}))
    (reset! hist-to-redo (peek @history))
    (session/put! :current-page target-page)))

;; Initialize the app
(defn ^:export init []
  (rfe/start! (rf/router routes)
              on-navigate
              {:use-fragment true})
  (reagent.dom/render
   [current-page]
   (. js/document (getElementById "app"))))
