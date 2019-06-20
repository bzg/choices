;; Copyright (c) 2019 Bastien Guerry <bzg@bzg.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSES/EPL-2.0.txt

(defproject choices "0.2.3"
  :description "Build SPAs to let users traverse choices"
  :url "https://github.com/bzg/choices"
  :license {:name "Eclipse Public License - v 2.0"
            :url  "http://www.eclipse.org/legal/epl-v20.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.520"]
                 [reagent "0.8.1"]
                 [compojure "1.6.1"]
                 [http-kit "2.3.0"]
                 [ring "1.7.1"]
                 [reagent-utils "0.3.3"]
                 [bidi "2.1.6"]
                 [venantius/accountant "0.2.4"]
                 [cljsjs/clipboard "2.0.4-0"]
                 [com.taoensso/tempura "1.2.1"]]
  :plugins [[lein-figwheel "0.5.18"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]]
  :source-paths ["src/clj"]
  :main choices.handler
  :jvm-opts ["-Xmx500m"]
  :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  :profiles {:uberjar {:aot :all}
             :dev     {:source-paths ["dev" "src/cljs"]
                       :plugins      [[lein-figwheel "0.5.18"]]}
             :repl    {:plugins      [[cider/cider-nrepl "0.18.0"]]
                       :dependencies [[nrepl "0.6.0"]
                                      [cider/piggieback "0.4.1"]
                                      [figwheel-sidecar "0.5.18"]]}}
  :cljsbuild {:builds
              {:dev
               {:source-paths ["src/cljs"]
                :figwheel     {:on-jsload      "choices.core/mount-root"
                               :websocket-host :js-client-host}
                :compiler     {:main                 choices.core
                               :asset-path           "/js/compiled/out"
                               :output-to            "resources/public/js/compiled/choices.js"
                               :output-dir           "resources/public/js/compiled/out"
                               :source-map-timestamp true}}
               :min
               {:source-paths ["src/cljs"]
                :compiler     {:output-to     "resources/public/js/compiled/choices.js"
                               :main          choices.core
                               :externs       ["externs.js"]
                               :optimizations :advanced
                               :pretty-print  false}}}}
  :figwheel {:http-server-root "public"
             :server-port      3449
             :server-ip        "0.0.0.0"
             :css-dirs         ["resources/public/css"]
             :ring-handler     choices.server/handler}
  :repl-options {:init-ns           choices.user
                 :skip-default-init false
                 :nrepl-middleware  [cider.piggieback/wrap-cljs-repl]})
