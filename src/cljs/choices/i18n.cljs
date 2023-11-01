;; Copyright (c) 2019-2023 Bastien Guerry <bzg@gnu.org>
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
    :mail-body-default    "Here is my feedback:\n"
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
    :mail-body            "Bonjour !\n%s\nMerci."
    :mail-body-default    "Voici mes retours :\n"
    :redo                 "Recommencer"
    :ok                   "D'accord"
    :contact-intro        "Contact : "
    :toggle-summary-style "Changer le style de résumé"
    :attention            "Attention"}
   :de
   {:display-help         "Anzeigehilfe"
    :copy-to-clipboard    "In die Zwischenablage kopieren"
    :mail-to-message      "Per E-Mail senden"
    :mail-subject         "Ergebnisse"
    :mail-body            "Hallo!\n%s\nDanke."
    :mail-body-default    "hier ist mein Feedback :\n"
    :redo                 "Neu anfangen"
    :ok                   "Einverstanden"
    :contact-intro        "Kontakt: "
    :toggle-summary-style "Den Stil der Zusammenfassung ändern"
    :attention            "Achtung"}
   })
