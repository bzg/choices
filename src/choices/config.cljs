(ns choices.config
  (:require [reagent.core :as reagent]))

(def mail-to "bzg@bzg.fr")

(def display-help false)

(def i18n {:display-help      {:fr "Afficher de l'aide"}
           :copy-to-clipboard {:fr "Copier dans le presse-papier"}
           :mail-to-message   {:fr "Envoyer par mail"}
           :mail-subject      {:fr "Résultat du guide open data"}
           :redo              {:fr "Recommencer"}})

(def header
  {:title    "Guide Open Data"
   :logo     "https://www.etalab.gouv.fr/wp-content/uploads/2014/07/logo-etalab-320x200.png"
   :color    "is-primary"
   :subtitle "Guide pour la publication des données administratives"})

(def footer
  {:text    "Une ligne d'explication sur le site."
   :contact "contact@me.info"})

(def input
  [{:name       "1"
    :start      true
    :text       "Êtes-vous une administration ?"
    :help       "On commence par la première question."
    :force-help true
    :choices    [{:answer  "Oui"
                  :explain "Une administration est ceci."
                  :summary "Vous êtes une administration."
                  :goto    "2"
                  :color   "is-success"}
                 {:answer  "Non"
                  :explain "Une administration n'est pas cela."
                  :summary "Vous n'êtes pas une administration."
                  :color   "is-warning"
                  :goto    "8"}]}
   {:name    "2"
    :text    "Vous avez plus de 50 agents ?"
    :help    "Levez la main."
    :choices [{:answer  "Oui, bien sûr."
               :goto    "3"
               :explain "Je les ai tous comptés."
               :summary "Vous avez plus de 50 agents."
               :color   "is-success"}
              {:answer  "En fait non."
               :goto    "8"
               :summary "Vous n'avez pas plus de 50 agents."
               :color   "is-warning"}]}
   {:name    "3"
    :text    "Vous avez envie de publier vos données ?"
    :help    "Ne soyez pas timides !"
    :choices [{:answer  "Oui, ce serait super !"
               :summary "Vous avez vraiment envie de publier vos données."
               :explain "Une explication."
               :goto    "8"
               :color   "is-success"}
              {:answer  "Non, j'ai trop peur."
               :summary "Vous avez trop peur de publier vos données."
               :explain "Encore une explication."
               :goto    "8"
               :color   "is-warning"}]}
   {:name "8"
    :text "Résulat des courses..."
    :done true}])
