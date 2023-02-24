(ns dactyl-node.core
  (:require [dactyl-generator.generator :as gen]
            [dactyl-generator.handler :as hd]))

(defn generate-manuform [config]
  (hd/generate-manuform (hd/api-generate-manuform (js->clj config)) true))

(defn generate-lightcycle [config]
  (hd/generate-lightcycle (hd/api-generate-lightcycle (js->clj config)) true))

(def defaultManuformState (clj->js (gen/generate-json-dm (hd/api-generate-manuform {}) true)))
(def defaultLightcycleState (clj->js (gen/generate-json-dl (hd/api-generate-lightcycle {}) true)))

(set! (.-onmessage js/self) )
