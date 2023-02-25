(defproject dactyl-node "0.1.0-SNAPSHOT"
  :description "Dactyl Generator in ClojureScript"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.773"]
                 [org.clojure/core.match "1.0.1"]
                 [net.mikera/core.matrix "0.63.0"]]

  :plugins [[lein-cljsbuild "1.1.7"]]

  ;; :source-paths ["src/cljs"]

  :cljsbuild {
    :builds {:worker {:source-paths ["src/cljs"]
                      :compiler {
                        :main "dactyl_worker.core"
                        :target :webworker
                        :output-to "target/dactyl_webworker.js"
                        ;; :source-map "target/dactyl_webworker.js.map"
                        :optimizations :advanced}}
             :node   {:source-paths ["src/cljs"]
                       :compiler {
                                   :main "dactyl_node.core"
                                   :target :node
                                   :output-to "target/dactyl_node.cjs"
                                   :optimizations :simple}}}})
