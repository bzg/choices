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

(defn use-dsfr [& [args]]
  (let [config    (slurp "config.yml")
        newconfig (string/replace config #"theme: \"[^\"]+\"" "theme: \"dsfr\"")]
    (spit "config.yml" newconfig)
    (io/copy
     (io/file "resources/public/index_dsfr.html")
     (io/file "resources/public/index.html"))))

(defn use-bulma [& [args]]
  (let [config    (slurp "config.yml")
        newconfig (string/replace config #"theme: \"[^\"]+\"" "theme: \"bulma\"")]
    (spit "config.yml" newconfig)
    (io/copy
     (io/file "resources/public/index_bulma.html")
     (io/file "resources/public/index.html"))))

(defn use-chota [& [args]]
  (let [config    (slurp "config.yml")
        newconfig (string/replace config #"theme: \"[^\"]+\"" "theme: \"chota\"")]
    (spit "config.yml" newconfig)
    (io/copy
     (io/file "resources/public/index_chota.html")
     (io/file "resources/public/index.html"))))

(defn set-theme [{:keys [theme]}]
  (let [theme (name theme)]
    (if-not theme
      (println "Please provide a theme")
      (cond (= theme "chota") (use-chota)
            (= theme "dsfr")  (use-dsfr)
            :else             (use-bulma)))))
