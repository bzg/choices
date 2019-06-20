;; Copyright (c) 2019 Bastien Guerry <bzg@bzg.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSES/EPL-2.0.txt
(ns choices.input)

;; Default page to display
(def home-page "0")

;; Start page to use when redoing the questionnaire
(def start-page "1")

;; Initial score, a map with strings and integers
;; For example: (def score {"happiness" 0 "cleverness" 0})
(def score {})

(def choices
  [{:name       "0"  ;; The webpage will look like /0
    :text       "A title for the default page"
    :force-help true ;; Always display help for this question
    :no-summary true ;; Don't display a summary for this start question
    :help       "Some help text"
    ;; The list of choices (with just one question here)
    :choices    [{:answer "Start now"
                  :goto   "1"
                  :color  "is-info"}]}
   
   {:name    "1"
    :text    "Is it the first question?"
    :help    "Some help text here."
    :choices [{:answer  "Yes"
               :summary "Yes, this is the first question."
               :goto    "2"
               :color   "is-info"}
              {:answer  "No"
               :summary "No, this is not the first question."
               :color   "is-warning"
               :goto    "end"}]}

   {:name    "2"
    :text    "Is it the first question (mhhh...) ?"
    :help    "Some help text here."
    :choices [{:answer  "Yes"
               :summary "Yes, this is the first question."
               :goto    "end"
               :color   "is-info"}
              {:answer  "No"
               :summary "No, this is not the first question."
               :color   "is-warning"
               :goto    "end"}]}
   
   {:name "end"
    :text "This is the end."
    :done true}])
