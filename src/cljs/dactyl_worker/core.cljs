(ns dactyl-node.worker
  (:require [dactyl-generator.generator :as gen]
            [dactyl-generator.handler :as hd]
            [scad-clj.scad :as scad]
            [scad-clj.jscadjs :as jscadjs]
            [goog.object :as g]))

(enable-console-print!)

(defn generate-csg [config]
  (->> (js->clj config)
      (hd/generate)
      (jscadjs/write-scad (g/get js/self "jscadModeling"))
      (clj->js)))

(defn generate-scad [config]
  (->> (js->clj config)
       (hd/generate)
       (scad/write-scad)))

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

(defn render [config]
  (let [ source (generate-scad config)
         openscad (g/get js/self "OpenSCAD")
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
      "csg" (try (message "csg" (generate-csg data))
                 (catch :default e (message "csgerror" e)))
      "scad" (message "scad" (generate-scad data))
      "stl" (message "stl" (render data)))))

(set! (.-onmessage js/self) on-message)
