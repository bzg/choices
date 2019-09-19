;; Copyright (c) 2019 DINSIC, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSES/EPL-2.0.txt
(ns choices.config)

;; Default locale for UI strings
(def locale "en-GB")

;; Customize UI strings
;; For example: (def ui-strings {:redo "Restart from scratch})
(def ui-strings {})

;; Allow users to send you emails with the summary?
(def mail-to "")

;; Display help along with questions by default?
(def display-help true)

;; Initial score, a map of strings and integers
;; For example: (def score {"happiness" 0 "cleverness" 0})
(def score {"Score for 1" 0
            "Score for 2" 0})

;; Whether to display the raw score values
(def display-score true)

;; Display content depending on scores
(defn score-function [scores]
  (let [s1 (get scores "Score for 1")
        s2 (get scores "Score for 2")]
    (when (> s2 0)
      [:div {:class "tile is-child is-warning notification is-12"}
       [:p "Score for 2 is more than 0!"]])))

;; Website header
(def header
  {:title    "Your title here"
   :logo     ""           ; ex: "/img/logo.png"
   :color    "is-primary" ; ex: has-background-white-ter
   :subtitle "A subtitle here, possibly with _markdown_ formatting."})

;; Website footer
(def footer
  {:text    "Some text here, possibly with **markdown** formatting."
   :contact ""}) ;; An email address

;; Decision tree, a map.
(def tree
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
    :help       "Some **help text** here for the first question.  Markdown formatting accepted."
    :start-page true ;; This is the page from where to restart.
    :choices    [{:answer  "Yes"
                  :summary "Yes, this is the first question."
                  :explain "Some explanation here."
                  :score   {"Score for 1" 1}
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
               :summary "Yes, this is the second question!"
               :score   {"Score for 2" 1}
               :goto    "3"
               :color   "is-info"}
              {:answer  "No"
               :summary "No, this is not the second question."
               :color   "is-warning"
               :goto    "end"}]}

   {:name    "3"
    :text    "Is it the _third_ question?"
    :help    "Some help text here for the second question."
    :choices [{:answer  "Yes"
               :summary ["Yes, this is the third question..."
                         "This will end soon, I promise."]
               :goto    "end"
               :color   "is-info"}
              {:answer  "No"
               :summary "No, this is not the third question."
               :color   "is-warning"
               :goto    "end"}]}
   
   {:name "end"
    :text "This is the end."
    :done true}])
