;; Copyright (c) 2019-2020 DINSIC, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSES/EPL-2.0.txt

(ns choices.i18n)

(def localization
  {:en
   {:display-help         "Display help"
    :copy-to-clipboard    "Copy in the clipboard"
    :mail-to-message      "Send by email"
    :mail-subject         "Results"
    :mail-body            "Hi,\n%s\nThanks."
    :redo                 "Redo"
    :ok                   "Okay"
    :contact-intro        "Contact: "
    :toggle-summary-style "Toggle summary style"
    :attention            "Attention"}
   :fr
   {:display-help         "Afficher de l'aide"
    :copy-to-clipboard    "Copier dans le presse-papier"
    :mail-to-message      "Envoyer par mail"
    :mail-subject         "Résultats"
    :mail-body            "Bonjour!\n%s\nMerci."
    :redo                 "Recommencer"
    :ok                   "D'accord"
    :contact-intro        "Contact : "
    :toggle-summary-style "Changer le style de résumé"
    :attention            "Attention"}})
