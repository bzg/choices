;; Copyright (c) 2019-2023 Bastien Guerry <bzg@gnu.org>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSES/EPL-2.0.txt

(ns choices.macros
  (:require [yaml.core :as yaml]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(defmacro inline-yaml-resource [resource-path]
  (yaml/parse-string
   (slurp resource-path)))

(defmacro inline-edn-resource [resource-path]
  (edn/read-string
   (slurp resource-path)))

(defn slurp-no-error [f]
  (try (slurp f) (catch Exception e nil)))

(defn use-theme [theme & [args]]
  (if-let [config (or (not-empty (slurp-no-error "config.yml"))
                      (slurp "config-example.yml"))]
    (let [index (slurp "resources/public/index.html")]
      (spit "config.yml"
            (string/replace config #"theme: \"[^\"]+\"" (format "theme: \"%s\"" theme)))
      (spit "resources/public/index.html"
            (string/replace index #"css/[^\"]+" (format "css/%s.css" theme)))
      (println "Choices theme set to" theme))))

(defn use-bulma [] (use-theme "bulma"))
(defn use-chota [] (use-theme "chota"))
(defn use-dsfr [] (use-theme "dsfr"))

(defn set-theme [{:keys [theme]}]
  (let [theme (name theme)]
    (if-not theme
      (println "Please provide a theme")
      (cond (= theme "chota") (use-chota)
            (= theme "dsfr")  (use-dsfr)
            :else             (use-bulma)))))
