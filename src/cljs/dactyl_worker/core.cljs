(ns dactyl-node.worker
  (:require [dactyl-generator.generator :as gen]
            [dactyl-generator.handler :as hd]))

(defn generate-manuform [config]
  (hd/generate-manuform (hd/api-generate-manuform (js->clj config)) true))

(defn generate-lightcycle [config]
  (hd/generate-lightcycle (hd/api-generate-lightcycle (js->clj config)) true))

(defn build-mesh [config] (.postMessage js/self (generate-manuform config)))

(defn load-mesh [msg] (build-mesh (.-data msg)))

(set! (.-onmessage js/self) load-mesh)
