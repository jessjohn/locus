;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(defproject com.7theta/locus "0.1.0"
  :description "A map component library."
  :url "https://github.com/7theta/locus"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.51"]

                 [reagent "0.6.0-alpha"]]
  :source-paths ["src/cljs"]
  :plugins [[lein-cljsbuild "1.0.6"]
            [lein-figwheel "0.3.3" :exclusions [cider/cider-nrepl
                                                org.clojure/clojure]]]
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  :profiles {:dev {:source-paths ["dev" "example/src"]
                   :resource-paths ["example/resources"]
                   :dependencies [[figwheel-sidecar "0.5.0-3"]
                                  [com.cemerick/piggieback "0.2.1"]]}}
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs"]
                        :resource-paths ["resources/public"]
                        :figwheel {:on-jsload "locus.core/mount-root"}
                        :warning-handlers [(fn [warning-type env extra]
                                             (when (warning-type cljs.analyzer/*cljs-warnings*)
                                               (when-let [s (cljs.analyzer/error-message warning-type extra)]
                                                 (binding [*out* *err*]
                                                   (println "WARNING:" (cljs.analyzer/message env s)))
                                                 (System/exit 1))))]
                        :compiler {:main locus.core
                                   :output-to "resources/public/js/compiled/app.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :asset-path "js/compiled/out"
                                   :source-map-timestamp true
                                   :optimizations :none
                                   :pretty-print  true}}
                       {:id "min"
                        :source-paths ["src/cljs"]
                        :compiler {:main locus.core
                                   :output-to "resources/public/js/compiled/app.js"
                                   :optimizations :advanced
                                   :pretty-print false}}]}
  :scm {:name "git"
        :url "https://github.com/7theta/locus"})
