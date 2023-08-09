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
(def t (macros/inline-edn-resource "theme.edn"))

;; Variables
(def show-help-global (reagent/atom (:display-help config)))
(def show-help (reagent/atom (:display-help config)))
(def show-modal (reagent/atom false))
(def show-summary-answers (reagent/atom true))
(def modal-message (reagent/atom ""))
(def show-summary (:display-summary config))
(def conditional-score-output (:conditional-score-output config))
(def sticky-help (reagent/atom ""))
(def score-variables (:score-variables config))
(def tree (:tree config))

;; home-page and start-page
(def home-page
  (first (remove nil? (map #(when (:home-page %) (keyword (:node %))) tree))))
(def start-page
  (first (remove nil? (map #(when (:start-page %) (keyword (:node %))) tree))))

(defn md-to-string [^string s]
  (-> s (md/md->hiccup) (md/component)))

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
      #(when-not (nil? @clipboard-atom)
         (reset! clipboard-atom nil))
      :reagent-render
      (fn []
        [:button.button.is-info.is-light.is-size-4
         {:title                 (i18n [:copy-to-clipboard])
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
  [:section
   [:div {:class (:header.section.div t)}
    [:div {:class (:container t)}
     [:div {:class (:columns t)}
      (let [logo (:logo (:header config))]
        (when (not-empty logo)
          [:div {:class (:column t)}
           [:figure {:class (:media-left t)}
            [:p {:class (:image t)}
             [:a {:href (rfe/href home-page)}
              [:img {:src logo}]]]]])
        [:div {:class
               (string/join
                " "
                (list (:column t)
                      (if (not-empty logo)
                        (:has-text-right t)
                        (:has-text-centered t))))}
         [:h1 {:class (:title t)} (:title (:header config))]
         [:br]
         [:h2 {:class (:subtitle t)}
          (md-to-string (:subtitle (:header config)))]])]]]])

(defn modal [show-modal]
  [:div {:class (str (:modal t) " " (when @show-modal (:is-active t)))}
   [:div {:class (:modal-background t)}]
   [:div {:class (:modal-content t)}
    [:div {:class (:box t)}
     [:div {:class (:title t)} (i18n [:attention])]
     @modal-message
     [:br]
     [:div {:class (:has-text-centered t)}
      [:a {:class    (string/join " " (list (:button t)
                                            (:is-medium t)
                                            (:is-warning t)))
           :on-click #(reset! show-modal false)}
       (i18n [:ok])]]]]
   [:button {:class (str (:modal-close t) " " (:is-large t))}
    {:aria-label "close"
     :on-click   #(reset! show-modal false)}]])

(defn question-help-clipboard [done question force-help help]
  [:div {:class (:level t)}
   [:div {:class (:is-size-3 t)}
    [:div {:class (str (:columns t) " " (:is-multiline t))}
     [:div {:class (:column t)}
      (md-to-string question)]]]
   (if-not done
     ;; Not done: display the help button
     (when (and (not force-help) @show-help-global
                (not-empty help))
       [:div {:class (:level-right t)}
        [:button
         {:class    (string/join " " (list (:level-item t)
                                           (:button t)
                                           (:is-info t)
                                           (:is-light t)
                                           (:is-size-4 t)))
          :title    (i18n [:display-help])
          :on-click #(swap! show-help not)}
         "💬"]])
     ;; Done: display the copy-to-clipboard button
     [:div {:class (:level-right t)}
      [:div {:class (:level-item t)}
       [:button
        {:class    (string/join " " (list
                                     (:button t)
                                     (:is-info t)
                                     (:is-light t)
                                     (:is-size-4 t)))
         :title    (i18n [:toggle-summary-style])
         :on-click #(swap! show-summary-answers not)}
        "🔗"]]
      [:div {:class (:level-item t)}
       [clipboard-button "📋" "#copy-this"]]])])

(defn footer []
  [:section {:class (:footer t)}
   [:div {:class (str (:content t) " " (:has-text-centered t))}
    (md-to-string (:text (:footer config)))
    (when-let [c (not-empty (:contact (:footer config)))]
      [:p (i18n [:contact-intro])
       [:a {:href (str "mailto:" c)} c]])]])

(defn score-details [scores]
  (for [row-score (partition-all 5 scores)]
    ^{:key (random-uuid)}
    [:div {:class (str (:tile t) " " (:is-ancestor t))}
     (for [s row-score]
       ^{:key (random-uuid)}
       [:div {:class (str (:tile t) " " (:is-parent t))}
        (let [v     (val s)
              value (:value v)]
          [:div {:class (str (:tile t) " " (:is-child-box t))}
           (str (:display v) ": " value)])])]))

(defn score-top-result [scores]
  (let [final-scores  (sort-map-by-score-values scores)
        last-score    (first final-scores)
        butlast-score (second final-scores)]
    (when (> (:value (val last-score)) (:value (val butlast-score)))
      (when-let [s (:as-top-result-display (val last-score))]
        [:div {:class (string/join " " (list (:tile t) (:is-parent t) (:is-6 t)))}
         [:p {:class (string/join " "
                                  (list
                                   (:tile t)
                                   (:is-child-box t)
                                   (:is-warning t)
                                   (:notification t)))}
          (:as-top-result-display s)]]))))

(defn conditional-score-result [scores conclusions]
  (let [conditions   (atom nil)
        matching     (atom nil)
        output       (atom "")
        notification (atom "")
        node         (atom "")]
    (doseq [[_ cas] conclusions
            :let    [not (:notification cas)
                     msg (:message cas)
                     nod (:node cas)
                     pri (:priority cas)
                     cds (dissoc cas :message :notification :priority)]]
      (doseq [condition cds]
        (swap! conditions conj
               (merge (val condition) {:msg msg :not not :pri pri :nod nod}))))
    (doseq [c0   @conditions
            :let [c  (dissoc c0 :msg :not :pri :nod)
                  ks (keys c)]]
      (when (all-vals-compare?
             (fn [a b] (if (zero? a) (= a b) (>= a b)))
             (select-keys scores ks) c)
        (swap! matching conj c0)))
    (let [match (first (sort-by :pri @matching))]
      (reset! output (:msg match))
      (reset! notification (:not match))
      (reset! node (:nod match)))
    ;; Return the expected map:
    {:notification @notification
     :output       @output
     :node         @node}))

(defn format-score-output-string [output scores]
  (let [scores
        (map (fn [[k v]]
               [(str "%" (name k))
                (when (:as-percent (get score-variables k))
                  (when-let [max (:max (get score-variables k))]
                    (fmt/format "%.0f" (/ (* v 100) max)))
                  v)])
             scores)]
    (reduce-kv string/replace output (into {} scores))))

(defn scores-result [scores]
  [:div
   (when (:display-score config)
     [:div {:class (:is-6 t)}
      ;; Optional, mainly for debugging purpose
      (when (:display-score-details config)
        (score-details scores))
      ;; Only when no conditional score output and
      ;; when :as-top-result-display is set for each score
      (when (and (not conditional-score-output)
                 (:display-score-top-result config))
        (score-top-result scores))
      ;; Only when score-results is defined
      (let [scores (apply merge (map (fn [[k v]] {k (:value v)}) scores))]
        (when conditional-score-output
          (let [{:keys [notification output]}
                (conditional-score-result
                 scores conditional-score-output)]
            (when (not-empty output)
              [:div {:class (str (:tile t) " " (:is-parent t))}
               [:div
                {:class (string/join " "
                                     (list (:tile t) (:is-size-4 t) (:is-child t)
                                           (or (not-empty notification) (:is-info t))
                                           (:notification t)
                                           (:subtitle t)))}
                (md-to-string
                 (format-score-output-string output scores))]]))))
      ;; Always display display-unconditionally when not empty
      (when-let [sticky (:display-unconditionally config)]
        [:div {:class (str (:tile t) " " (:is-parent t))}
         [:div
          {:class (string/join " "
                               (list (:tile t) (:is-size-4 t) (:is-child t)
                                     (:notification t)
                                     (:subtitle t)))}
          (md-to-string sticky)]])])
   [:br]])

(defn summary []
  (let [divclass (string/join " " (list (:tile t)
                                        (:is-parent t)
                                        (:is-horizontal t)
                                        (:notification t)))]
    (for [o (if @show-summary-answers
              (remove nil? (reverse (:answers (peek @history))))
              (remove nil? (reverse (:questions (peek @history)))))]
      ^{:key (random-uuid)}
      (cond
        (and (string? o) (not-empty o))
        [:div
         {:class divclass
          :key   (random-uuid)}
         [:div {:class (str (:title t) " " (:is-child t))}
          [:div {:class (:subtitle t)} (md-to-string o)]]]
        (not-empty (butlast o))
        [:div
         {:class divclass
          :key   (random-uuid)}
         (for [n (butlast o)]
           (when (not-empty n)
             [:div
              {:class (string/join " " (list (:tile t) (:is-child t) (:subtitle t)))
               :key   (random-uuid)}
              (md-to-string n)]))
         (when-let [a (not-empty (peek o))]
           [:div
            {:class (string/join " "
                                 (list
                                  (:tile t)
                                  (:is-child t)
                                  (:subtitle t)
                                  (:has-text-centered t)
                                  (:has-text-weight-bold t)
                                  (:is-size-4 t)))
             :key   (random-uuid)}
            (md-to-string a)])]))))

(defn restart-mailto-buttons []
  (let [divclass (string/join " " (list (:button t)
                                        (:level-item t)
                                        (:is-info t)
                                        (:is-light t)
                                        (:is-size-4 t)))]
    [:div {:class (:level-right t)}
     [:a {:class divclass
          :title (i18n [:redo])
          :href  (rfe/href start-page)} "🔃"]
     (when (not-empty (:mail-to config))
       (let [contents (or (not-empty (remove nil? (:answers (peek @history))))
                          (not-empty (remove nil? (:questions (peek @history))))
                          (i18n [:mail-body-default]))
             body     (->> contents flatten
                           (map strip-html-tags)
                           (string/join "%0D%0A%0D%0A")
                           (fmt/format (i18n [:mail-body])))]
         [:a
          {:class divclass
           :title (i18n [:mail-to-message])
           :href  (str "mailto:" (:mail-to config)
                       "?subject=" (i18n [:mail-subject])
                       "&body="
                       (string/replace body #"[\n\t]" "%0D%0A%0D%0A"))}
          "📩"]))]))

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
            (when-let [sticky-help-msg (:stick-help result)]
              (reset! sticky-help sticky-help-msg))
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
                      ;; one boolean and/or one string, take last:
                      :else b_v))})
   previous_score current_score))

(defn display-choices [choices text no-summary]
  (doall
   (for [choices-row (partition-all 4 choices)]
     ^{:key (random-uuid)}
     [:div {:class (string/join " " (list (:tile t) (:is-parent t) (:is-horizontal t)))}
      (doall
       (for [{:keys [answer goto explain color summary score]} choices-row]
         ^{:key (random-uuid)}
         [:div
          {:class (string/join " " (list (:tile t) (:is-child t)
                                         (when (> (count choices) 3) (:is-3 t))))}
          [:a
           {:class (:tile t)
            :style {:text-decoration "none"}
            :on-click
            #(do (when (vector? summary)
                   (reset! show-modal true)
                   (reset! modal-message (md-to-string (peek summary))))
                 (let [current-score
                       (merge-scores (:score (peek @history)) score)]
                   (reset! hist-to-add
                           (merge
                            {:score current-score}
                            {:questions (when-not no-summary [text answer])}
                            {:answers summary}))
                   (rfe/push-state
                    (get-target-node goto current-score))))}
           [:div {:class (string/join " " (list (:card-content t)
                                                (:tile t)
                                                (:is-parent t)
                                                (:is-vertical t)))}
            [:div {:class (string/join " " (list (:tile t)
                                                 (:is-child t)
                                                 (:box t)
                                                 (:is-size-4 t)
                                                 (:notification t)
                                                 (:has-text-centered t)
                                                 (or (not-empty color) (:is-info t))))}
             (md-to-string answer)]
            (when (and explain @show-help)
              [:div {:class (string/join " " (list (:tile t) (:is-child t) (:subtitle t)))}
               (md-to-string explain)])]]]))])))

;; Create all the pages
(defn create-page-contents [{:keys [done node text help no-summary
                                    progress force-help choices]}]
  (let [divclass (str (:notification t) "  " (:is-size-5 t))]
    (defmethod page-contents (keyword node) []
      [:div
       (when (not-empty (:header config)) (header))
       [:div {:class (:container t)}
        (modal show-modal)
        [:div.section
         (when-let [[v m] (cljs.reader/read-string progress)]
           [:div [:progress {:class (str (:progress t) " " (:is-success t))
                             :value v :max m}]
            [:br]])
         (when-let [sticky-help-message (not-empty @sticky-help)]
           [:div {:class divclass}
            {:class (or (:sticky-help-color config)
                        (:is-warning t))}
            (md-to-string sticky-help-message)])
         (question-help-clipboard done text force-help help)
         (when (or force-help @show-help)
           (when-let [help-message (not-empty help)]
             [:div {:class divclass}
              (md-to-string help-message)]))
         (if-not done
           ;; Not done: display the choices
           [:div
            {:class (string/join " " (list
                                      (:tile t)
                                      (:is-ancestor t)
                                      (:is-vertical t)))}
            (display-choices choices text no-summary)]
           ;; Done: display the final summary-answers
           [:div
            [:div {:class (str (:tile t) " " (:is-ancestor t))
                   :id    "copy-this"}
             [:div
              {:class (string/join " " (list
                                        (:tile t)
                                        (:is-parent t)
                                        (:is-vertical t)
                                        (:is-12 t)))}
              ;; Display score
              (if-let [scores (:score (peek @history))]
                (scores-result scores)
                [:br])
              ;; Display answers
              (when show-summary (summary))]]
            (restart-mailto-buttons)])]]
       (when (not-empty (:footer config))
         (footer))])))

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
  (rfe/start!
   (rf/router routes)
   on-navigate
   {:use-fragment true})
  (reagent.dom/render
   [current-page]
   (. js/document (getElementById "app"))))
