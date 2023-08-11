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

(defn use-bulma [& [args]]
  (if-let [config (or (not-empty (slurp-no-error "config.yml"))
                      (slurp "config-example.yml"))]
    (let [index (slurp "resources/public/index.html")]
      (spit "config.yml"
            (string/replace config #"theme: \"[^\"]+\"" "theme: \"bulma\""))
      (spit "resources/public/index.html"
            (string/replace index #"css/[^\"]+" "css/bulma.css")))
    (println "Can't set bulma theme")))

(defn use-chota [& [args]]
  (if-let [config (or (not-empty (slurp-no-error "config.yml"))
                      (slurp "config-example.yml"))]
    (let [index (slurp "resources/public/index.html")]
      (spit "config.yml"
            (string/replace config #"theme: \"[^\"]+\"" "theme: \"chota\""))
      (spit "resources/public/index.html"
            (string/replace index #"css/[^\"]+" "css/chota.css")))
    (println "Can't set chota theme")))

(defn set-theme [{:keys [theme]}]
  (let [theme (name theme)]
    (if-not theme
      (println "Please provide a theme")
      (if (= theme "chota")
        (use-chota)
        (use-bulma)))))
