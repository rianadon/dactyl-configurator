(ns dactyl-node.web
  (:require [dactyl-generator.generator :as gen]
            [dactyl-generator.handler :as hd]
            [dactyl-generator.manuform :as dm]
            [scad-clj.scad :as scad]
            [scad-clj.jscad :as jscad]
            [scad-clj.jscadjs :as jscadjs]
            [goog.object :as g]))

(def generate (comp hd/generate js->clj))
(def generate-scad (comp scad/write-scad generate))

(defn generate-js [config modeling]
  (clj->js (jscadjs/write-scad modeling (generate config))))

(defn generate-manifold [config modeling]
  (clj->js (jscadjs/write-expr modeling (generate config))))
