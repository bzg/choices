(ns choices.test
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure.spec.alpha :as s]
   [choices.macros :refer [inline-yaml-resource]]))

(def config (inline-yaml-resource "config.yml"))
(def score-variables (:score-variables config))
(def conditional-score-output (:conditional-score-output config))
(defn nilable-email? [s] (s/valid? ::email s))
(defn nilable-map? [m] (s/valid? (s/nilable map?) m))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Define specs

(s/def ::node string?)
(s/def ::text string?)
(s/def ::progress string?)
(s/def ::doc string?)
(s/def ::answer string?)
(s/def ::explain string?)
(s/def ::goto (s/or :target-node string?
                    :conditional-target-node map?)) ;; FIXME: be more specific?
(s/def ::color string?)
(s/def ::summary (s/or :simple-summary string?
                       :composed-summary (s/coll-of string?)))
(s/def ::help string?)
(s/def ::home-page boolean?)
(s/def ::start-page boolean?)
(s/def ::no-summary boolean?)
(s/def ::done boolean?)

(s/def ::display string?)
(s/def ::value (s/or :float float? :int int?))
(s/def ::as-top-result-display string?)

(s/def ::score-variable (s/keys :req-un [::display ::value]
                                :opt-un [::as-top-result-display]))
(s/def ::score-variables (s/map-of keyword? ::score-variable))
(s/def ::choice-score (s/keys :req-un [::value]))
(s/def ::score (s/map-of keyword? ::choice-score))

(s/def ::choice (s/keys :req-un [::answer ::goto]
                        :opt-un [::color ::summary ::score ::explain]))
(s/def ::choices (s/coll-of ::choice))

(s/def ::priority int?)
(s/def ::message string?)
(s/def ::notification string?)

(s/def ::condition (s/keys :req-un [::message]
                           :opt-un [::priority ::node ::notification]))
(s/def ::output-branch (s/tuple keyword? ::condition))
(s/def ::conditional-score-output (s/coll-of ::output-branch))

(s/def ::branch (s/keys :req-un [::node ::text]
                        :opt-un [::choices ::home-page ::start-page ::progress
                                 ::help ::no-summary ::done]))
(s/def ::tree (s/coll-of ::branch))

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def ::email (s/or :empty nil? :with-arobase #(re-matches email-regex %)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Testing config.yml

(deftest ui
  (testing "Testing UI variables"
    (is (or (= (:locale config) "en")
            (= (:locale config) "fr")
            (= (:locale config) "sv")))
    (is (nilable-map? (:ui-strings config)))
    (is (nilable-email? (:mail-to config)))
    (is (boolean? (:display-summary config)))
    (is (boolean? (:display-score config)))
    (is (boolean? (:display-score-details config)))
    (is (boolean? (:display-score-top-result config)))
    (is (map? (:header config)))
    (is (map? (:footer config)))))

(deftest outputs
  (testing "Testing score setting"
    (is (s/valid? ::conditional-score-output conditional-score-output))))

(deftest score
  (testing "Testing score setting"
    (is (s/valid? ::score-variables score-variables))))

(deftest tree
  (testing "Testing the options tree format"
    (is (s/valid? ::tree (:tree config)))))

