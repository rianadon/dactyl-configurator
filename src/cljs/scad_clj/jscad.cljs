(ns scad-clj.jscad
  (:require [clojure.string :refer [join]]
            [scad-clj.model :refer [rad->deg]]
            [clojure.core.match :refer [match]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; multimethod

;; Join arrays together by separators. Discard nil arrays.
(defn joinc [sep x]
  (reduce (fn [a b] (if (nil? a) b
                        (if (nil? b) a (concat a sep b)))) x))

(defmulti write-expr
  (fn [depth [form & args]]
    (if (keyword? form) form :list)))

(defmethod write-expr :default [depth [form & args]]
  `("//(" ~form ~args ")"))

(defmethod write-expr :list [depth [& args]]
  (joinc ",/**/\n" (map #(write-expr depth %1) args)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; utility

(defn indent [depth]
  (join (repeat depth "  ")))

(defn write-block [depth block]
  (joinc ",\n" (map #(write-expr (inc depth) %1) block)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Modifier

(defmethod write-expr :modifier [depth [form modifier & block]]
  (concat
   (list (indent depth) modifier "union (\n")
   (write-block depth block)
   (list (indent depth) ")\n")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; include and call into scad libraries.

(declare map-to-arg-string)

(defn make-arguments [args]
  (let [arg (first args)
        rest (rest args)
        piece (cond
               (map? arg) (map-to-arg-string arg)
               (coll? arg) (str "[" (make-arguments arg) "]")
               :else arg)]
    (if (empty? rest)
      piece
      (join ", " [piece (make-arguments rest)]))))

(defn map-to-arg-string [m]
  (join ", " (map (fn [[k v]] (str (name k) "=" (make-arguments [v])) ) m)))

(defmethod write-expr :include [depth [form {:keys [library]}]]
  (list (indent depth) "include <" library">\n"))

(defmethod write-expr :use [depth [form {:keys [library]}]]
  (list (indent depth) "use <" library">\n"))

(defmethod write-expr :import [depth [form file]]
  (list (indent depth) "import (\"" file "\");\n"))

(defmethod write-expr :call [depth [form {:keys [function]} & args]]
  (list (indent depth) function "(" (make-arguments (apply vec args)) ");\n"))

(defmethod write-expr :call-module-with-block [depth [form {:keys [module]} & args]]
  (let [the-args (butlast (first args))
        block (list (last (first args)))]
    (concat
     (list (indent depth) module " (" (make-arguments (vec the-args)) ") {\n")
     (write-block depth block)
     (list (indent depth) "}\n"))))

(defmethod write-expr :call-module-no-block [depth [form {:keys [module]} & args]]
  (let [the-args (first args)]
    (list (indent depth) module " (" (make-arguments (vec the-args)) ");\n")))

(defmethod write-expr :define-module [depth [form {:keys [module]} & args]]
  (let [the-args (butlast (first args))
        block (list (last (first args)))]
    (concat
     (list (indent depth) "module " module "(" (make-arguments (vec the-args)) ") {\n")
     (write-block depth block)
     (list (indent depth) "};\n"))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 2D

(defmethod write-expr :circle [depth [form {:keys [r fa fn fs center]}]]
  (let [fargs (str (and fa (str "$fa=" fa ", "))
                   (and fn (str "$fn=" fn ", "))
                   (and fs (str "$fs=" fs ", ")))]
    (list (indent depth) "circle (" fargs "r=" r ");")))

(defmethod write-expr :square [depth [form {:keys [x y center]}]]
  (list (indent depth) "square ({size:[" x ", " y "]"
        (when (not center) "BAD, center:true") "})"))

(defmethod write-expr :polygon [depth [form {:keys [points paths convexity]}]]
  `(~@(indent depth) "polygon ({"
    "points:[[" ~(join "], [" (map #(join ", " %1) points)) "]]"
    ~@(when paths [", paths:[[" (join "], [" (map #(join "," %1) paths)) "]]"])
    ~@(when convexity [", convexity:" convexity])
    "})"))

(defmethod write-expr :text [depth [form {:keys [text size font halign valign spacing direction language script fn]}]]
  (list (indent depth) "text (\"" text "\""
        (when fn (str ", $fn=" fn))
        (when size (str ", size=" size))
        (when font (str ", font=\"" font "\""))
        (when halign (str ", halign=\"" halign "\""))
        (when valign (str ", valign=\"" valign "\""))
        (when spacing (str ", spacing=" spacing))
        (when direction (str ", direction=\"" direction "\""))
        (when language (str ", language=\"" language "\""))
        (when script (str ", script=\"" script "\""))");"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 3D

(defmethod write-expr :sphere [depth [form {:keys [r fa fn fs center]}]]
  (let [fargs (str (and fa (str "fa:" fa ", "))
                   (and fn (str "fn:" fn ", "))
                   (and fs (str "fs:" fs ", ")))]
    (list (indent depth) "sphere({" fargs "radius:" r "})")))

(defmethod write-expr :cube [depth [form {:keys [x y z center]}]]
  (list (indent depth) "cuboid({size: [" x ", " y ", " z "]"
        (when (not center) "BAD, center: true") "})"))

(defmethod write-expr :cylinder [depth [form {:keys [h r r1 r2 fa fn fs center]}]]
  (let [fargs (str (and fa (str "fa:" fa ", "))
                   (and fn (str "fn:" fn ", "))
                   (and fs (str "fs:" fs ", ")))]
    (concat
     (if r
       (list (indent depth) "cylinder({" fargs "height:" h ", radius:" r)
       (list (indent depth) "cylinderElliptic({" fargs "height:" h ", startRadius:[" r1 "," r1 "], endRadius: [" r2 "," r2 "]" ))
     (when (not center) (list "BAD, center:true"))
     (list "})"))))

(defmethod write-expr :polyhedron [depth [form {:keys [points faces convexity]}]]
  `(~@(indent depth) "polyhedron({"
    "points:[[" ~(join "], [" (map #(join ", " %1) points)) "]], "
    "faces:[[" ~(join "], [" (map #(join ", " %1) faces)) "]]"
    ~@(if (nil? convexity) [] [", convexity:" convexity])
    "})"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; transformations

(defmethod write-expr :resize [depth [form {:keys [x y z auto]} & block]]
  (concat
   (list (indent depth) "resize ([" x ", " y ", " z "]")
   (list (when-not (nil? auto)
           (str " auto="
                (if (coll? auto)
                  (str "[" (join ", " (map true? auto)) "]")
                  (true? auto)))))
   "){\n"
   (mapcat #(write-expr (inc depth) %1) block)
   (list (indent depth) "}\n")))

(defmethod write-expr :translate [depth [form [x y z] & block]]
  (concat
   (list (indent depth) "translate ([" x ", " y ", " z "],\n")
   (write-block depth block)
   (list "\n" (indent depth) ")")))

;; (def rodrigues [a [x y z]]


(defmethod write-expr :rotatev [depth [form [a [x y z]] & block]]
  (concat
   (match [x y z]
     [1 0 0] (list (indent depth) "rotateX(" a ",\n")
     [0 1 0] (list (indent depth) "rotateY(" a ",\n")
     [0 0 1] (list (indent depth) "rotateZ(" a ",\n")
     :else   (list (indent depth) "rotate(" a ", [" x ", " y ", " z "],\n"))
   (write-block depth block)
   (list "\n" (indent depth) ")")))

(defmethod write-expr :rotatec [depth [form [x y z] & block]]
  (concat
   (list (indent depth) "rotate([" x "," y "," z "],\n")
   (write-block depth block)
   (list "\n" (indent depth) ")")))

(defmethod write-expr :scale [depth [form [x y z] & block]]
  (concat
   (list (indent depth) "scale([" x ", " y ", " z "],\n")
   (write-block depth block)
   (list "\n" (indent depth) ")")))

(defmethod write-expr :mirror [depth [form [x y z] & block]]
  (concat
   (list (indent depth) "mirror({normal:[" x ", " y ", " z "]},\n")
   (write-block depth block)
   (list "\n" (indent depth) ")")))

(defmethod write-expr :color [depth [form [r g b a] & block]]
  (concat
    (list (indent depth) "colorize([" r ", " g ", " b ", " a"],\n")
    (write-block depth block)
    (list "\n" (indent depth) ")")))

(defmethod write-expr :hull [depth [form & block]]
  (concat
   (list (indent depth) "hull(\n")
   (write-block depth block)
   (list "\n" (indent depth) ")")))

(defmethod write-expr :offset
  [depth [form {:keys [r delta chamfer] :or {chamfer false}} & block]]
  (concat
   (list (indent depth) "offset (")
   (if r
     (list "r = " r)
     (list "delta = " delta))
   (when chamfer (list ", chamfer=true"))
   (list ") {\n")
   (mapcat #(write-expr (inc depth) %1) block)
   (list (indent depth) "}\n")))

(defmethod write-expr :minkowski [depth [form & block]]
  (concat
   (list (indent depth) "minkowski () {\n")
   (mapcat #(write-expr (inc depth) %1) block)
   (list (indent depth) "}\n")))

(defmethod write-expr :multmatrix [depth [form m & block]]
  (let [w (fn [s] (str "[" s "]")) ;; wrap
        co (fn [c] (apply str (interpose "," c)))] ;; put commas in between
    (concat
     (list (indent depth) "multmatrix(")
     (w (co (map #(w (co %)) m)))
     (list ") {\n")
     (mapcat #(write-expr (inc depth) %1) block)
     (list (indent depth) "}\n"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Boolean operations

(defmethod write-expr :union [depth [form & block]]
  (concat
   (list (indent depth) "union(\n")
   (write-block depth block)
   (list "\n" (indent depth) ")")))

(defmethod write-expr :difference [depth [form & block]]
  (concat
   (list (indent depth) "subtract(\n")
   (write-block depth block)
   (list "\n" (indent depth) ")")))

(defmethod write-expr :intersection [depth [form & block]]
  (concat
   (list (indent depth) "intersection(\n")
   (write-block depth block)
   (list "\n" (indent depth) ")")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; other

(defmethod write-expr :surface [depth [form {:keys [filepath convexity center invert]}]]
  (concat
   (list (indent depth) "surface (file = \"" filepath "\""
         (when convexity (str ", convexity=" convexity))
         (when center ", center=true")
         (when invert ", invert=true")
         ");\n")))

(defmethod write-expr :projection [depth [form {:keys [cut]} & block]]
  (concat
   (list (indent depth) "project({cut: " cut "},\n")
   (mapcat #(write-expr (inc depth) %1) block)
   (list "\n" (indent depth) ")")))

(defmethod write-expr :extrude-linear [depth [form {:keys [height twist convexity center slices scale]} & block]]
  (concat
   (list (indent depth) "extrudeLinear ({height:" height)
   (if (nil? twist) [] (list ", twist:" (rad->deg twist)))
   (if (nil? convexity) [] (list ", convexity:" convexity))
   (if (nil? slices) [] (list ", slices:" slices))
   (cond
     (nil? scale) []
     (sequential? scale) (list ", scale:[" (first scale) ", " (second scale) "]")
     :else (list ", scale:" scale))
   (when (not center) (list "BAD, center:true"))
   (list "},\n")

   (mapcat #(write-expr (inc depth) %1) block)
   (list "\n" (indent depth) ")")))

(defmethod write-expr :extrude-rotate [depth [form {:keys [convexity fn angle]} & block]]
  (concat
   (list (indent depth) "rotate_extrude ({")
   (join ", "
     (concat
       (if convexity [(str "convexity:" convexity)])
       (if angle [(str "angle:" angle)])
       (if fn [(str "fn:" fn)])))
   (list "),")
   (mapcat #(write-expr (inc depth) %1) block)
   (list "\n" (indent depth) ")")))

(defmethod write-expr :render [depth [form {:keys [convexity]} & block]]
  (concat
   (list (indent depth) (str "render (convexity=" convexity ") {\n"))
   (mapcat #(write-expr (inc depth) %1) block)
   (list (indent depth) "}\n")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; special variables

(defmethod write-expr :fa [depth [form x]]
  (list (indent depth) "$fa = " x ";\n"))

(defmethod write-expr :fn [depth [form x]]
  (list (indent depth) "$fn = " x ";\n"))

(defmethod write-expr :fs [depth [form x]]
  (list (indent depth) "$fs = " x ";\n"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; output

(defn write-scad [& block]
  (join (concat
         (list
          "const { circle, square, cuboid, sphere, cylinder, cylinderElliptic } = require('@jscad/modeling').primitives\n"
          "const { rotate, rotateX, rotateY, rotateZ, scale, translate, mirror } = require('@jscad/modeling').transforms\n"
          "const { intersection, subtract, union } = require('@jscad/modeling').booleans\n"
          "const { extrudeLinear, project } = require('@jscad/modeling').extrusions\n"
          "const { hull } = require('@jscad/modeling').hulls\n"
          "const { colorize } = require('@jscad/modeling').colors\n"
          "module.exports.main = () => "
          )
         (write-expr 0 block))))
