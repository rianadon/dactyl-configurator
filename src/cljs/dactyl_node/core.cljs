(ns dactyl-node.core
  (:require [dactyl-generator.generator :as gen]
            [dactyl-generator.handler :as hd]
            [dactyl-generator.manuform :as dm]
            [scad-clj.scad :as scad]
            [scad-clj.jscad :as jscad]
            [scad-clj.jscadjs :as jscadjs]
            [goog.object :as g]))

(def generate (comp hd/generate js->clj))
(def generate-scad (comp scad/write-scad generate))
(def generate-jscad (comp jscad/write-scad generate))

(defn generate-js [config modeling]
  (clj->js (jscadjs/write-scad modeling (generate config))))

(g/set js/exports "generateSCAD" generate-scad)
(g/set js/exports "generateJSCAD" generate-jscad)
(g/set js/exports "generateJS" generate-js)

(g/set js/exports "radToAngle" hd/rad-to-angle)
(g/set js/exports "degToAngle" hd/deg-to-angle)

(g/set js/exports "defaultManuformState" (clj->js (gen/generate-json-dm (hd/api-generate-manuform {}) true)))
(g/set js/exports "defaultLightcycleState" (clj->js (gen/generate-json-dl (hd/api-generate-lightcycle {}) true)))
