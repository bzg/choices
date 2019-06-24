;; Copyright (c) 2019 DINSIC, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSES/EPL-2.0.txt
(ns choices.view)

;; Allow users to send you emails with the summary?
(def mail-to "")

;; Display help along with questions by default?
(def display-help true)

;; Default locale for UI strings
(def locale "en-GB")

;; Customize UI strings
;; For example: (def ui-strings {:redo "Restart from scratch})
(def ui-strings {})

;; Website header
(def header
  {:title    "Your title here"
   :logo     "" ;; "/img/logo.png"
   :color    "is-primary"
   :subtitle "Your subtitle here"})

;; Website footer
(def footer
  {:text    "Some text here."
   :contact ""}) ;; An email address
