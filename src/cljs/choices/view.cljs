(ns choices.view)

(def mail-to "bzg@bzg.fr")

(def display-help true)

(def ui-strings {:display-help         "Afficher de l'aide"
                 :copy-to-clipboard    "Copier dans le presse-papier"
                 :mail-to-message      "Envoyer par mail"
                 :mail-subject         "Résultat du guide open data"
                 :redo                 "Recommencer"
                 :ok                   "D'accord"
                 :contact-intro        "Contact : "
                 :toggle-summary-style "Changer le style de résumé"
                 :attention            "Attention"
                 :404-title            "Page introuvable (erreur 404)"
                 :404-subtitle         ""})

(def header
  {:title    "Guide juridique de l'Open Data"
   :logo     "/img/logo-etalab-370x250.png"
   :color    "is-primary"
   :subtitle "Guide pour la publication des données administratives"})

(def footer
  {:text    [:div "Ce site a été réalisée par la mission " [:a {:href "https://www.etalab.gouv.fr/"} "Etalab"]", à partir du " [:a {:href "https://www.cada.fr/lacada/consultation-publique-sur-le-guide-pratique-de-la-publication-en-ligne-et-de-la-reutilisation"} "Guide pratique de la publication en ligne et de la réutilisation des données publiques"] " rédigé par la CADA et la CNIL."]
   :contact "info@data.gouv.fr"})
