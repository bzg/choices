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
  {:title    "Guide juridique de l'Open Data"
   :logo     "https://www.etalab.gouv.fr/wp-content/uploads/2014/07/logo-etalab-320x200.png"
   :color    "is-primary"
   :subtitle "Guide pour la publication des données administratives"})

(def footer
  {:text    "L’application a été réalisée par la mission Etalab, à partir du Guide pratique de la publication en ligne et de la réutilisation des données publiques rédigé par la CADA et la CNIL."
   :contact "info@data.gouv.fr"})

(def input
  [{:name       "0"
    :start      true
    :text       "Une présentation du guide"
    :force-help true
    :help       "Un assez long text ici"
    :choices    [{:answer "Commencer"
                  :goto   "1"
                  :color  "is-success"}]}

   {:name    "1"
    :text    "Votre document est-il achevé ?"
    :help    "Un document administratif correspond à tout document produit ou reçu par une administration dans le cadre de sa mission de service public. Un document administratif produit peut être une base de données contenant des informations relatives à une mission de service public. Un document reçu peut être un document fourni par une administration à une autre pour les besoins de sa mission de service public."
    :choices [{:answer  "Oui"
               :summary "Votre document est achevé."
               :goto    "2"
               :color   "is-success"}
              {:answer  "Non"
               :summary "Votre document n'est pas achevé."
               :color   "is-warning"
               :goto    "non"}]}
   
   {:name    "2"
    :text    "Le document contient-il des données couvertes par un secret légal ?"
    :help    "Un secret légal correspond aux documents administratifs dont la consultation ou la communication porterait atteinte au secret des délibérations du Gouvernement et des autorités responsables relevant du pouvoir exécutif, au secret de la défense nationale, à la conduite de la politique extérieure de la France, à la sûreté de l'Etat, à la sécurité publique, à la sécurité des personnes ou à la sécurité des systèmes d'information des administrations, à la monnaie et au crédit public, au déroulement des procédures engagées devant les juridictions, à la recherche et à la prévention d'infractions de toute nature. Ne sont également pas communicables les documents administratifs dont la communication porterait atteinte à la protection de la vie privée, au secret médical et au secret des affaires. Enfin les documents administratifs portant une appréciation ou un jugement de valeur sur une personne physique ou faisant apparaitre le comportement d'une personne qui lui porterait préjudice ne sont pas communicables."
    :choices [{:answer  "Oui"
               :goto    "6"
               :summary "Le document contient des données couvertes par un secret légal."
               :color   "is-success"}
              {:answer  "Non"
               :goto    "3"
               :summary "Le document ne contient pas de données couvertes par un secret légal."
               :color   "is-warning"}]}
   
   {:name    "3"
    :text    "Le document contient-il des données à caractère personnel ?"
    :help    "Une donnée à caractère personnel correspond à toute information relative à une personne physique identifiée ou qui peut être identifiée, directement ou indirectement, par référence à un numéro d'identification ou à un ou plusieurs éléments qui lui sont propres."
    :choices [{:answer  "Oui"
               :summary "Le document contient des données à caractère personnel."
               :goto    "5"
               :color   "is-success"}
              {:answer  "Non"
               :summary "Le document ne contient pas de données à caractère personnel."
               :goto    "oui"
               :color   "is-warning"}]}

   ;; {:name    "4"
   ;;  :text    "Ces données peuvent-elles être occultées par un traitement automatisé d’usage courant sans que cela ne dénature ni ne vide de son sens le document ?"
   ;;  :help    "Un document est « dénaturé » ou « vidé » de son sens s’il ne contient plus de données ou si les données-clés pour la compréhension du document sont enlevées."
   ;;  :choices [{:answer  "Oui"
   ;;             :summary "Vous pouvez occulter vos données."
   ;;             :goto    "3"
   ;;             :color   "is-success"}
   ;;            {:answer  "Non"
   ;;             :summary "Vous ne pouvez pas occulter vos données"
   ;;             :goto    "non"
   ;;             :color   "is-warning"}]}
   
   ;; {:name    "5"
   ;;  :text    "Ces données sont-elles nécessaires à l’information du public ?"
   ;;  :help    "Les données « nécessaires à l’information du public » ont été définies par le Décret n° 2018-1117 du 10 décembre 2018 relatif aux catégories de documents administratifs pouvant être rendus publics sans faire l'objet d'un processus d'anonymisation. Il s’agit, notamment, de données relatives à l’organisation de l’administration (ex : annuaires administratifs), aux conditions d’exercice et d’organisation de la vie politique, aux conditions d’exercice des professions réglementées, ou encore aux conditions d’organisation et d’exercice des activités touristiques."
   ;;  :choices [{:answer  "Oui"
   ;;             :summary "Ces données sont nécessaires à l’information du public."
   ;;             :goto    "oui"
   ;;             :color   "is-success"}
   ;;            {:answer  "Non"
   ;;             :summary "Ces données ne sont pas nécessaires à l’information du public."
   ;;             :goto    "6"
   ;;             :color   "is-warning"}]}

   ;; {:name    "6"
   ;;  :text    "Ces données sont-elles nécessaires à l’information du public ?"
   ;;  :help    "Les données « nécessaires à l’information du public » ont été définies par le Décret n° 2018-1117 du 10 décembre 2018 relatif aux catégories de documents administratifs pouvant être rendus publics sans faire l'objet d'un processus d'anonymisation. Il s’agit, notamment, de données relatives à l’organisation de l’administration (ex : annuaires administratifs), aux conditions d’exercice et d’organisation de la vie politique, aux conditions d’exercice des professions réglementées, ou encore aux conditions d’organisation et d’exercice des activités touristiques."
   ;;  :choices [{:answer  "Oui"
   ;;             :summary "Ces données sont nécessaires à l’information du public."
   ;;             :goto    "oui"
   ;;             :color   "is-success"}
   ;;            {:answer  "Non"
   ;;             :summary "Ces données ne sont pas nécessaires à l’information du public."
   ;;             :goto    "6"
   ;;             :color   "is-warning"}]}

   ;; {:name    "6"
   ;;  :text    "Ces données peuvent-elles être anonymisées sans que cette opération implique des efforts disproportionnés ou que le document ne soit dénaturé ou vidé de son sens ?"
   ;;  :help    "L'anonymisation est un processus consistant à traiter des données à caractère personnel afin d’empêcher totalement et de manière irréversible l’identification d’une personne physique. L’anonymisation suppose donc qu’il n’y ait plus aucun lien possible entre l’information concernée et la personne à laquelle elle se rattache. L’identification devient alors totalement impossible. L'anonymisation doit être adaptée à chaque jeu de données. La notion d’efforts disproportionnés est laissée à l’appréciation de chaque administration ; on considère que le retrait d’une colonne ou d’une ligne d’une base de données ne constitue pas un effort disproportionné."
   ;;  :choices [{:answer  "Oui"
   ;;             :summary "Ces données peuvent être anonymisées comme requis."
   ;;             :goto    "oui"
   ;;             :color   "is-success"}
   ;;            {:answer  "Non"
   ;;             :summary "Ces données ne peuvent pas être anonymisées comme requis."
   ;;             :goto    "non"
   ;;             :color   "is-warning"}]}

   ;; {:name    "6"
   ;;  :text    "Ces données peuvent-elles être anonymisées sans que cette opération implique des efforts disproportionnés ou que le document ne soit dénaturé ou vidé de son sens ?"
   ;;  :help    "L'anonymisation est un processus consistant à traiter des données à caractère personnel afin d’empêcher totalement et de manière irréversible l’identification d’une personne physique. L’anonymisation suppose donc qu’il n’y ait plus aucun lien possible entre l’information concernée et la personne à laquelle elle se rattache. L’identification devient alors totalement impossible. L'anonymisation doit être adaptée à chaque jeu de données. La notion d’efforts disproportionnés est laissée à l’appréciation de chaque administration ; on considère que le retrait d’une colonne ou d’une ligne d’une base de données ne constitue pas un effort disproportionné."
   ;;  :choices [{:answer  "Oui"
   ;;             :summary "Ces données peuvent être anonymisées comme requis."
   ;;             :goto    "oui"
   ;;             :color   "is-success"}
   ;;            {:answer  "Non"
   ;;             :summary "Ces données ne peuvent pas être anonymisées comme requis."
   ;;             :goto    "non"
   ;;             :color   "is-warning"}]}
   
   {:name "non"
    :text "Vous n’êtes pas tenus de publier votre document en Open data."
    :done true}
   
   {:name "oui"
    :text "Vous devez publier votre document en Open data sur une plateforme dédiée !"
    :done true}
   ])
