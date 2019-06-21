;; Copyright (c) 2019 DINSIC, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSES/EPL-2.0.txt

(ns choices.handler
  (:require [clojure.java.io :as io]
            [ring.util.response :as response]
            [org.httpkit.server :as server]
            [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [not-found resources]])
  (:gen-class))

(defn default-page []
  (assoc
   (response/response
    (io/input-stream
     (io/resource
      "public/index.html")))
   :headers {"Content-Type" "text/html; charset=utf-8"}))

(defroutes routes
  (GET "/" [] (default-page))
  (GET "/:page" [page] (default-page))
  (resources "/")
  (not-found "Not Found"))

(defn -main [& args]
  (let [port (read-string (or (System/getenv "CHOICES_PORT") "3000"))]
    (server/run-server #'routes {:port port})
    (println (str "Choices application started on localhost:" port))))

