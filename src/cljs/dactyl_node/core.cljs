(ns dactyl-node.core
  (:require [dactyl-generator.generator :as gen]
            [dactyl-generator.handler :as hd]
            [goog.object :as g]))

(defn generate-manuform [config]
  (hd/generate-manuform (hd/api-generate-manuform (js->clj config)) true))

(defn generate-lightcycle [config]
  (hd/generate-lightcycle (hd/api-generate-lightcycle (js->clj config)) true))

(g/set js/exports "generateManuform" generate-manuform)
(g/set js/exports "generateLightcyle" generate-lightcycle)
(g/set js/exports "defaultManuformState" (clj->js (gen/generate-json-dm (hd/api-generate-manuform {}) true)))
(g/set js/exports "defaultLightcycleState" (clj->js (gen/generate-json-dl (hd/api-generate-lightcycle {}) true)))
