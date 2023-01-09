;; Copyright (c) 2019-2023 Bastien Guerry <bzg@gnu.org>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSES/EPL-2.0.txt

(ns choices.macros
  (:require [yaml.core :as yaml]))

(defmacro inline-yaml-resource [resource-path]
  (yaml/parse-string
   (slurp resource-path)))
