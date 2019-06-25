(ns choices.test-runner
  (:require
   [choices.core-test]
   [figwheel.main.testing :refer [run-tests-async]]))

(defn -main [& args]
  (run-tests-async 5000))
