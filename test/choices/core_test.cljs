(ns choices.core-test
  (:require
   [clojure.spec.alpha :as s]
   [cljs.test :refer-macros [deftest is testing]]
   [choices.config :as config]))

(s/def ::name string?)
(s/def ::text string?)
(s/def ::answer string?)
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
                        :opt-un [::color ::summary ::score]))

(s/def ::tree (s/coll-of ::branch))
(s/def ::branch (s/keys :req-un [::name ::text]
                        :opt-un [::choices ::home-page ::start-page
                                 ::help ::no-summary ::done]))

(deftest ui-tests
  (testing "Testing basic UI variables"
    (is (or (= config/locale "en-GB")
            (= config/locale "fr-FR")))
    (is (map? config/ui-strings))
    (is (string? config/mail-to))
    (is (boolean? config/display-help))
    (is (map? config/score))
    (is (map? config/header))
    (is (map? config/footer))))

(deftest tree-tests
  (testing "Testing tree format"
    (is (s/valid? ::tree config/tree))))
