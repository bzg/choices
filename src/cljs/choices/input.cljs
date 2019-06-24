;; Copyright (c) 2019 DINSIC, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSES/EPL-2.0.txt
(ns choices.input)

;; Initial score, a map of strings and integers
;; For example: (def score {"happiness" 0 "cleverness" 0})
(def score {})

;; Available choices, a map.
(def choices
  [{:name       "0"  ;; The webpage will look like yourdomain.com/#/0
    :text       "A title for the default page"
    :home-page  true ;; This is the home page of the application
    :force-help true ;; Always display help for this question
    :no-summary true ;; Don't display a summary for this start question
    :help       "Some introductory text here."
    :choices    ;; The list of choices (with just one question here)
    [{:answer "Start now"
      :goto   "1"
      :color  "is-info"}]}
   
   {:name       "1"
    :text       "Is it the first question?"
    :help       "Some help text here for the first question."
    :start-page true ;; This is the page from where to restart.
    :choices    [{:answer  "Yes"
                  :summary "Yes, this is the first question."
                  :goto    "2"
                  :color   "is-info"}
                 {:answer  "No"
                  :summary "No, this is not the first question."
                  :color   "is-warning"
                  :goto    "end"}]}

   {:name    "2"
    :text    "Is it the second question?"
    :help    "Some help text here for the second question."
    :choices [{:answer  "Yes"
               :summary "Yes, this is the second question."
               :goto    "end"
               :color   "is-info"}
              {:answer  "No"
               :summary "No, this is not the second question."
               :color   "is-warning"
               :goto    "end"}]}
   
   {:name "end"
    :text "This is the end."
    :done true}])
