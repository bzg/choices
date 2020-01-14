(ns choices.test
  (:require
   [clojure.test :refer :all]
   [clojure.spec.alpha :as s]
   [choices.macros :refer [inline-yaml-resource]]))

(def config (inline-yaml-resource "config.yml"))

(s/def ::name string?)
(s/def ::text string?)
(s/def ::answer string?)
(s/def ::explain string?)
(s/def ::goto string?)
(s/def ::color string?)
(s/def ::summary (s/or :simple-summary string?
                       :composed-summary (s/coll-of string?)))
(s/def ::help string?)
(s/def ::score map?)
(s/def ::home-page boolean?)
(s/def ::start-page boolean?)
(s/def ::no-summary boolean?)
(s/def ::done boolean?)

(s/def ::choices (s/coll-of ::choice))
(s/def ::choice (s/keys :req-un [::answer ::goto]
                        :opt-un [::color ::summary ::score ::explain]))

(s/def ::tree (s/coll-of ::branch))
(s/def ::branch (s/keys :req-un [::name ::text]
                        :opt-un [::choices ::home-page ::start-page
                                 ::help ::no-summary ::done]))

(deftest ui-tests
  (testing "Testing basic UI variables"
    (is (or (= (:locale config) "en")
            (= (:locale config) "fr")))
    (is (or (empty? (:ui-strings config))
            (map? (:ui-strings config))))
    (is (or (empty? (:mail-to config))
            (re-find #"@" (:mail-to config))))
    (is (boolean? (:display-help config)))
    (is (boolean? (:display-score config)))
    (is (map? (:init-scores config)))
    (is (map? (:header config)))
    (is (map? (:footer config)))))

(deftest tree-tests
  (testing "Testing tree format"
    (is (s/valid? ::tree (:tree config)))))
