(defproject choices "0.1.0"
  :description "Build SPAs to let users traverse choices"
  :url "https://github.com/bzg/choices"
  :license {:name "Eclipse Public License - v 2.0"
            :url  "http://www.eclipse.org/legal/epl-v20.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.520"]
                 [reagent "0.8.1"]
                 [reagent-utils "0.3.3"]
                 [bidi "2.1.6"]
                 [venantius/accountant "0.2.4"]
                 [cljsjs/clipboard "2.0.4-0"]]
  :plugins [[lein-figwheel "0.5.18"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]]
  :source-paths ["src"]
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  :profiles {:dev
             {:source-paths ["dev"]
              :plugins      [[lein-figwheel "0.5.18"]]}
             :repl {:plugins      [[cider/cider-nrepl "0.18.0"]]
                    :dependencies [[nrepl "0.6.0"]
                                   [cider/piggieback "0.4.0"]
                                   [figwheel-sidecar "0.5.18"]]}}
  :cljsbuild {:builds
              {:dev
               {:source-paths ["src"]
                :figwheel     {:on-jsload      "choices.core/on-js-reload"
                               :websocket-host :js-client-host}
                :compiler     {:main                 choices.core
                               :asset-path           "/js/compiled/out"
                               :output-to            "resources/public/js/compiled/choices.js"
                               :output-dir           "resources/public/js/compiled/out"
                               :source-map-timestamp true}}
               :min
               {:source-paths ["src"]
                :compiler     {:output-to     "resources/public/js/compiled/choices.js"
                               :main          choices.core
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
