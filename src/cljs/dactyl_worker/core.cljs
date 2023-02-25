(ns dactyl-node.worker
  (:require [dactyl-generator.generator :as gen]
            [dactyl-generator.handler :as hd]
            [dactyl-generator.manuform :as dm]
            [scad-clj.jscadjs :as jscadjs]
            [goog.object :as g]))

(enable-console-print!)

(defn generate-manuform [config]
  (println "CONFIG" (js->clj config))
  (println "CONFIG" (hd/api-generate-manuform (js->clj config)))
  (hd/generate-manuform (hd/api-generate-manuform (js->clj config)) true))

(defn generate-lightcycle [config]
  (hd/generate-lightcycle (hd/api-generate-lightcycle (js->clj config)) true))

(defn generate-manuform-js-js [config]
  (let [ conf (hd/api-generate-manuform (js->clj config)) ]
    (clj->js (jscadjs/write-scad (g/get js/self "jscadModeling") (dm/model-right conf)))))

(defn message [type data]
  (.postMessage js/self #js { :type type :data data }))

(defn load-scripts [urls]
  (.importScripts js/self urls)
  (message "scriptsinit" nil))

(defn load-wasm  [urls]
  (g/set js/self "OpenSCAD" #js { "noInitialRun" true
                                  "locateFile" #(second urls)
                                  "onRuntimeInitialized" #(message "wasminit" nil)
                                  "print" #(message "log" %1)
                                  "printErr" #(message "log" %1) })
  (.importScripts js/self (first urls)))

(defn render [source]
  (let [ openscad (g/get js/self "OpenSCAD")
         fs (g/get openscad "FS") ]
    ((g/get fs "writeFile") "/source.scad" source)
    ((g/get openscad "callMain") (array "/source.scad" "-o" "out.stl" ))
    ((g/get fs "readFile") "/out.stl")))

(defn on-message [msg]
  (let [ type (.. msg -data -type)
         data (.. msg -data -data)]
    (js/console.log (.-data msg))
    (case type
      "scripts" (load-scripts data)
      "wasm" (load-wasm data)
      "mesh" (message "csg" (generate-manuform-js-js data))
      "scad" (message "scad" (generate-manuform data))
      "render" (message "stl" (render data)))))

(set! (.-onmessage js/self) on-message)
