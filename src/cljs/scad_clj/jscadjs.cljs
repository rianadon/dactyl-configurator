(ns scad-clj.jscadjs
  (:require [clojure.string :refer [join]]
            [scad-clj.model :refer [rad->deg]]
            [clojure.core.match :refer [match]]
            [dactyl-generator.util :refer [rodrigues]]
            [goog.object :as g]
            ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; multimethod

(defmulti write-expr
  (fn [modeling [form & args]]
    (if (keyword? form) form :list)))

(defmethod write-expr :default [modeling [form & args]]
  (throw (js/Error. "Unsupported :default")))

(defmethod write-expr :list [modeling [& args]]
  ;; (apply (g/get (g/get modeling "booleans") "union")
   (map #(write-expr modeling %1) args))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; utility

(defn write-block [modeling block]
  (clj->js (map #(write-expr modeling %1) block)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Modifier

(defmethod write-expr :modifier [modeling [form modifier & block]]
  (throw (js/Error. "Unsupported :modifier")))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 2D

(defmethod write-expr :circle [modeling [form {:keys [r fa fn fs center]}]]
  (if-not center (throw (js/Error. "Circle's center must be true")))
  ((g/get (g/get modeling "primitives") "circle")
   (clj->js (merge
             { "radius" r }
             (when fa { "fa" fa })
             (when fn { "fn" fn })
             (when fs { "fs" fs })))))

(defmethod write-expr :square [modeling [form {:keys [x y center]}]]
  (if-not center (throw (js/Error. "Square's must be true")))
  ((g/get (g/get modeling "primitives") "square")
   (clj->js { "size" [x y] })))

;; (defmethod write-expr :polygon [modeling [form {:keys [points paths convexity]}]]
  ;; `(~@(indent modeling) "polygon ({"
    ;; "points:[[" ~(join "], [" (map #(join ", " %1) points)) "]]"
    ;; ~@(when paths [", paths:[[" (join "], [" (map #(join "," %1) paths)) "]]"])
    ;; ~@(when convexity [", convexity:" convexity])
    ;; "})"))

;; (defmethod write-expr :text [modeling [form {:keys [text size font halign valign spacing direction language script fn]}]]
  ;; (list (indent modeling) "text (\"" text "\""
        ;; (when fn (str ", $fn=" fn))
        ;; (when size (str ", size=" size))
        ;; (when font (str ", font=\"" font "\""))
        ;; (when halign (str ", halign=\"" halign "\""))
        ;; (when valign (str ", valign=\"" valign "\""))
        ;; (when spacing (str ", spacing=" spacing))
        ;; (when direction (str ", direction=\"" direction "\""))
        ;; (when language (str ", language=\"" language "\""))
        ;; (when script (str ", script=\"" script "\""))");"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 3D

(defmethod write-expr :sphere [modeling [form {:keys [r fa fn fs]}]]
  ((g/get (g/get modeling "primitives") "sphere")
   (clj->js (merge
             { "radius" r }
             (when fa { "fa" fa })
             (when fn { "fn" fn })
             (when fs { "fs" fs })))))

(defmethod write-expr :cube [modeling [form {:keys [x y z center]}]]
  (if-not center (throw (js/Error. "Cube's center must be true")))
  ((g/get (g/get modeling "primitives") "cuboid")
   (clj->js { "size" [x y z] })))

(defmethod write-expr :cylinder [modeling [form {:keys [h r r1 r2 fa fn fs center]}]]
  (if-not center (throw (js/Error. "Cylinder's center must be true")))
  (let [primitives (g/get modeling "primitives")
        fargs (merge (when fa { "fa" fa })
                     (when fn { "fn" fn })
                     (when fs { "fs" fs }))]
    (if r
      ((g/get primitives "cylinder") (merge { "height" h "radius" r } fargs))
      ((g/get primitives "cylinderElliptic") (merge { "height" h "startRadius" [r1 r1] "endRadius" [r2 r2] } fargs)))))

;; (defmethod write-expr :polyhedron [modeling [form {:keys [points faces convexity]}]]
  ;; `(~@(indent modeling) "polyhedron({"
    ;; "points:[[" ~(join "], [" (map #(join ", " %1) points)) "]], "
    ;; "faces:[[" ~(join "], [" (map #(join ", " %1) faces)) "]]"
    ;; ~@(if (nil? convexity) [] [", convexity:" convexity])
    ;; "})"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; transformations

;; (defmethod write-expr :resize [modeling [form {:keys [x y z auto]} & block]]
  ;; (concat
   ;; (list (indent modeling) "resize ([" x ", " y ", " z "]")
   ;; (list (when-not (nil? auto)
           ;; (str " auto="
                ;; (if (coll? auto)
                  ;; (str "[" (join ", " (map true? auto)) "]")
                  ;; (true? auto)))))
   ;; "){\n"
   ;; (mapcat #(write-expr modeling %1) block)
   ;; (list (indent modeling) "}\n")))

(defmethod write-expr :translate [modeling [form [x y z] & block]]
  ((g/get (g/get modeling "transforms") "translate")
   (array x y z) (write-block modeling block)))

(defmethod write-expr :rotatev [modeling [form [a [x y z]] & block]]
  (let [transforms (g/get modeling "transforms")
        mat4 (g/get (g/get modeling "maths") "mat4")
        inside (write-block modeling block)
        matrix ((g/get mat4 "fromRotation") ((g/get mat4 "create")) a [x y z])]
   (match [x y z]
     [1 0 0] ((g/get transforms "rotateX") a inside)
     [0 1 0] ((g/get transforms "rotateY") a inside)
     [0 0 1] ((g/get transforms "rotateZ") a inside)
     :else   ((g/get transforms "transform") matrix inside))))

(defmethod write-expr :rotatec [modeling [form [x y z] & block]]
  ((g/get (g/get modeling "transforms") "rotate")
   (array x y z) (write-block modeling block)))

(defmethod write-expr :scale [modeling [form [x y z] & block]]
  ((g/get (g/get modeling "transforms") "scale")
   (array x y z) (write-block modeling block)))

(defmethod write-expr :mirror [modeling [form [x y z] & block]]
  ((g/get (g/get modeling "transforms") "mirror")
   (clj->js { "normal" [x y z] }) (write-block modeling block)))

(defmethod write-expr :color [modeling [form [r g b a] & block]]
  ((g/get (g/get modeling "colors") "colorize")
   (array r g b a) (write-block modeling block)))

(defmethod write-expr :hull [modeling [form & block]]
  ((g/get (g/get modeling "hulls") "hull")
   (write-block modeling block)))

;; (defmethod write-expr :offset
;;   [modeling [form {:keys [r delta chamfer] :or {chamfer false}} & block]]
;;   (concat
;;    (list (indent modeling) "offset (")
;;    (if r
;;      (list "r = " r)
;;      (list "delta = " delta))
;;    (when chamfer (list ", chamfer=true"))
;;    (list ") {\n")
;;    (mapcat #(write-expr modeling %1) block)
;;    (list (indent modeling) "}\n")))

;; (defmethod write-expr :minkowski [modeling [form & block]]
;;   (concat
;;    (list (indent modeling) "minkowski () {\n")
;;    (mapcat #(write-expr modeling %1) block)
;;    (list (indent modeling) "}\n")))

;; (defmethod write-expr :multmatrix [modeling [form m & block]]
;;   (let [w (fn [s] (str "[" s "]")) ;; wrap
;;         co (fn [c] (apply str (interpose "," c)))] ;; put commas in between
;;     (concat
;;      (list (indent modeling) "multmatrix(")
;;      (w (co (map #(w (co %)) m)))
;;      (list ") {\n")
;;      (mapcat #(write-expr modeling %1) block)
;;      (list (indent modeling) "}\n"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Boolean operations

(defmethod write-expr :union [modeling [form & block]]
  (apply (g/get (g/get modeling "booleans") "union")
    (write-block modeling block)))

(defmethod write-expr :difference [modeling [form & block]]
  (apply (g/get (g/get modeling "booleans") "subtract")
   (write-block modeling block)))

(defmethod write-expr :intersection [modeling [form & block]]
  (apply (g/get (g/get modeling "booleans") "intersection")
   (write-block modeling block)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; other

;; (defmethod write-expr :surface [modeling [form {:keys [filepath convexity center invert]}]]
;;   (concat
;;    (list (indent modeling) "surface (file = \"" filepath "\""
;;          (when convexity (str ", convexity=" convexity))
;;          (when center ", center=true")
;;          (when invert ", invert=true")
;;          ");\n")))

(defmethod write-expr :projection [modeling [form {:keys [cut]} & block]]
  ((g/get (g/get modeling "extrusions") "project")
   (clj->js { "cut" cut }) (write-block modeling block)))

(defmethod write-expr :extrude-linear [modeling [form {:keys [height twist convexity center slices scale]} & block]]
  (if-not center (throw (js/Error. "Extrude's center must be true")))
  ;; (if convexity (throw (js/Error. "Convexity not supported")))
  (if scale (if-not (= scale 1) (throw (js/Error. "Scale not supported"))))
  ((g/get (g/get modeling "extrusions") "extrudeLinear")
   (clj->js (merge
             { "height" height }
             (when twist { "twistAngle" twist })
             (when slices { "twistSteps" slices })))
   (write-block modeling block)))

;; (defmethod write-expr :extrude-rotate [modeling [form {:keys [convexity fn angle]} & block]]
;;   (concat
;;    (list (indent modeling) "rotate_extrude ({")
;;    (join ", "
;;      (concat
;;        (if convexity [(str "convexity:" convexity)])
;;        (if angle [(str "angle:" angle)])
;;        (if fn [(str "fn:" fn)])))
;;    (list "),")
;;    (mapcat #(write-expr modeling %1) block)
;;    (list "\n" (indent modeling) ")")))

;; (defmethod write-expr :render [modeling [form {:keys [convexity]} & block]]
;;   (concat
;;    (list (indent modeling) (str "render (convexity=" convexity ") {\n"))
;;    (mapcat #(write-expr modeling %1) block)
;;    (list (indent modeling) "}\n")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; special variables

;; (defmethod write-expr :fa [modeling [form x]]
;;   (list (indent modeling) "$fa = " x ";\n"))

;; (defmethod write-expr :fn [modeling [form x]]
;;   (list (indent modeling) "$fn = " x ";\n"))

;; (defmethod write-expr :fs [modeling [form x]]
;;   (list (indent modeling) "$fs = " x ";\n"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; output

(defn get-polygons [modeling model]
  #js { "polygons" ((g/get (g/get (g/get modeling "geometries") "geom3") "toPolygons")
    model) })

(defn write-scad [modeling & block]
  (map #(get-polygons modeling %1) (write-expr modeling block)))
