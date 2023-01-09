;; Copyright (c) 2019-2023 Bastien Guerry <bzg@gnu.org>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSES/EPL-2.0.txt

(defproject choices "0.9.5"

  :description "Build SPAs to let users traverse choices"
  :url "https://github.com/etalab/choices"
  :license {:name "Eclipse Public License - v 2.0"
            :url  "http://www.eclipse.org/legal/epl-v20.html"}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [io.forward/yaml "1.0.10"]]

  ;; See https://www.deps.co/blog/how-to-upgrade-clojure-projects-to-use-java-11/
  :managed-dependencies [[org.clojure/core.rrb-vector "0.1.1"]
                         [org.flatland/ordered "1.5.9"]]

  :clean-targets ^{:protect false} ["target" "resources/public/js/dev/"
                                    "resources/public/js/choices.js"]

  :aliases {"fig"       ["trampoline" "run" "-m" "figwheel.main"]
            "fig:build" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]
            "fig:min"   ["run" "-m" "figwheel.main" "-O" "advanced" "-bo" "dev"]}

  :source-paths ["src/clj" "src/cljs"]
  
  :profiles {:dev {:source-paths ["src/cljs"]
                   :dependencies [[org.clojure/clojurescript "1.10.597"]
                                  [markdown-to-hiccup "0.6.2"]
                                  [com.bhauman/figwheel-main "0.2.8"]
                                  [com.bhauman/rebel-readline-cljs "0.1.4"]
                                  [reagent "0.10.0"]
                                  [reagent-utils "0.3.3"]
                                  [cljsjs/clipboard "2.0.4-0"]
                                  [com.taoensso/tempura "1.2.1"]
                                  [metosin/reitit-frontend "0.5.2"]]}})
