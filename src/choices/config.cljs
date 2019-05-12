(ns choices.config
  (:require [reagent.core :as reagent]))

(def help true)

(def header
  {:title    "Guide Open Data"
   :color    "is-primary"
   :subtitle "Guide pour la publication des données administratives"})

(def footer
  {:text    "Ce site web est super."
   :contact "bastien.guerry@data.gouv.fr"})

(def input
  [{:name    "1"
    :start   true
    :text    "Êtes-vous une administration ?"
    :help    "La bonne question au bon moment."
    :choices [{:answer  "Oui"
               :explain "Une administration est blabla."
               :summary "Vous êtes une administration."
               :goto    "2"
               :color   "is-primary"}
              {:answer  "Non"
               :explain "Une administration n'est pas blabla."
               :summary "Vous n'êtes pas une administration."
               :color   "is-info"
               :goto    "8"}]}
   {:name    "2"
    :text    "Vous avez plus de 50 agents ?"
    :help    "Levez la main."
    :choices [{:answer  "Oui !"
               :goto    "3"
               :explain "Un agent est une blabla."
               :summary "Vous avez plus de 50 agents."
               :color   "is-primary"}
              {:answer  "En fait non."
               :goto    "8"
               :summary "Vous n'avez pas plus de 50 agents."
               :color   "is-danger"}]}
   {:name    "3"
    :text    "Vous avez envie de publier vos données ?"
    :help    "Ne soyez pas timides !"
    :choices [{:answer  "Oui, à fond !!!"
               :summary "Vous avez à fond envie de publier vos données."
               :explain "Je ne sais pas trop."
               :goto    "8"
               :color   "is-danger"}
              {:answer  "Ben en fait je m'en double-tamponne."
               :summary "Vous vous double-tamponnez de publier vos données."
               :explain "Encore une explication."
               :goto    "8"
               :color   "is-primary"}]}
   {:name            "8"
    :text            "Voilà, c'est fini !"
    :done            true
    :display-summary false
    :mail-summary    false}])
