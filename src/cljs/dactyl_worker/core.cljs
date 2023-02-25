(ns dactyl-node.worker
  (:require [dactyl-generator.generator :as gen]
            [dactyl-generator.handler :as hd]
            [goog.object :as g]))

(enable-console-print!)

(defn generate-manuform [config]
  (println "CONFIG" (js->clj config))
  (println "CONFIG" (hd/api-generate-manuform (js->clj config)))
  (hd/generate-manuform (hd/api-generate-manuform (js->clj config)) true))

(defn generate-lightcycle [config]
  (hd/generate-lightcycle (hd/api-generate-lightcycle (js->clj config)) true))

(defn message [type data]
  (.postMessage js/self #js { :type type :data data }))

(defn render [source]
  (let [ openscad (g/get js/self "OpenSCAD")
         fs (g/get openscad "FS") ]
    ((g/get fs "writeFile") "/source.scad" source)
    ((g/get openscad "callMain") (array
                                      "/source.scad"
                                      "-o"
                                      "out.stl",
                                      ;; "--enable=fast-csg"
                                      ;; "--enable=fast-csg-trust-corefinement"
                                      ;; "--enable=fast-csg-remesh"
                                      ;; "--enable=lazy-union"
                                      ))
    ((g/get fs "readFile") "/out.stl")))

(defn load-mesh [config]
  (let [scad (generate-manuform config)]
    (message "scad" scad)
    (message "stl" (render scad))))

(defn load-scripts [urls]
  (g/set js/self "OpenSCAD" #js {
                                  "noInitialRun" true
                                  "locateFile" (fn [] (second urls))
                                  "onRuntimeInitialized" (fn [] (message "init" nil))
                                  "print" (fn [text] (message "log" text))
                                  "printErr" (fn [text] (message "log" text))
                                  })
  (.importScripts js/self (first urls)))

(defn on-message [msg]
  (let [ type (.. msg -data -type)
         data (.. msg -data -data)]
    (js/console.log (.-data msg))
    (case type
      "scripts" (load-scripts data)
      "mesh" (load-mesh data))))

(set! (.-onmessage js/self) on-message)
