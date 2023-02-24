(ns dactyl-generator.util
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.model :refer [union hull project extrude-linear translate]]))

(defn deg2rad [degrees]
  (* (/ degrees 180) Math/PI))

(defn bottom [height p]
  (->> (project p)
       (extrude-linear {:height height :twist 0 :convexity 0})
       (translate [0 0 (- (/ height 2) 0)])))

(defn bottom-hull [& p]
  (hull p (bottom 0.001 p)))

;; takes a list of 'location's,
;; partitions them into triad,
;; and apply hull on each triad,
;; then finally apply union for the result.
(defn triangle-hulls
  "It creates a wall that borders with the 'location's in the list."
  [& shapes]
  (apply union
         (map (partial apply hull)
              (partition 3 1 shapes))))

(def π Math/PI)
