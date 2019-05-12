(ns choices.server)

(defn handler [request]
  {:status 200
   :body   (slurp "resources/public/index.html")})
