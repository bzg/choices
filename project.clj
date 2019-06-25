;; Copyright (c) 2019 DINSIC, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSES/EPL-2.0.txt

(defproject choices "0.7.0"

  :description "Build SPAs to let users traverse choices"
  :url "https://github.com/etalab/choices"
  :license {:name "Eclipse Public License - v 2.0"
            :url  "http://www.eclipse.org/legal/epl-v20.html"}

  :dependencies [[org.clojure/clojure "1.10.1"]]

  :clean-targets ^{:protect false} ["target" "resources/public/js/"]

  :aliases {"fig"       ["trampoline" "run" "-m" "figwheel.main"]
            "fig:build" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]
            "fig:min"   ["run" "-m" "figwheel.main" "-O" "advanced" "-bo" "dev"]
            "fig:test"  ["run" "-m" "figwheel.main" "-co" "test.cljs.edn" "-m" choices.test-runner]}
  
  :profiles {:dev {:source-paths ["src"]
                   :dependencies [[org.clojure/clojurescript "1.10.520"]
                                  [markdown-to-hiccup "0.6.2"]
                                  [com.bhauman/figwheel-main "0.2.1"]
                                  [com.bhauman/rebel-readline-cljs "0.1.4"]
                                  [reagent "0.8.1"]
                                  [reagent-utils "0.3.3"]
                                  [cljsjs/clipboard "2.0.4-0"]
                                  [com.taoensso/tempura "1.2.1"]
                                  [metosin/reitit-frontend "0.3.9"]]}})
