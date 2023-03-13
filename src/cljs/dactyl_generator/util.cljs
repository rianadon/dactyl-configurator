(ns dactyl-generator.util
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.model :refer [union hull project extrude-linear translate]]))

(defn deg2rad [degrees]
  (* (/ degrees 180) Math/PI))

(defn bottom [height p transl]
  (->> (project p)
       (extrude-linear {:height height :twist 0 :convexity 0})
       (translate [0 0 (- (/ height 2) transl)])))

(defn bottom-hull-t [transl & p]
  (hull p (bottom 0.001 p transl)))

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


(defn vdot [[a b c] [d e f]] (+ (* a d) (* b e) (* c f)))
(defn vadd [& vs] (apply map + vs))

;; Matrix times column vector
(defn mmul [[r1 r2 r3] x]
  [(vdot r1 x) (vdot r2 x) (vdot r3 x)])

(defn transpose [[[a b c] [d e f] [g h i]]]
  [[a d g] [b e h] [c f i]])

;; Matrix times a matrix
(defn mmmul [[r1 r2 r3] M]
  (let [Mt (transpose M)]
    [(mmul Mt r1) (mmul Mt r2) (mmul Mt r3)]))

;; Scalar multiplication
(defn smul [x M] (mmmul [[x 0 0] [0 x 0] [0 0 x]] M))

;; Matrix addition
(defn madd [& Ms] (apply map vadd Ms))

;; Rotation matrix for a rotation of angle a around a vetor v
(defn rodrigues [a v]
  (let [[x y z] v
        K [[0 (- z) y] [z 0 (- x)] [(- y) x 0]]]
    (madd [[1 0 0] [0 1 0] [0 0 1]]
          (smul (Math/sin a) K)
          (smul (- 1 (Math/cos a)) (mmmul K K)))))
