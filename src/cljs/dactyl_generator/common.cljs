(ns dactyl-generator.common
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.model :refer [translate cube hull rotate cylinder union mirror extrude-linear polygon sphere difference color sphere-fn cylinder-fn project *fn*]]
            [dactyl-generator.util :refer [mmul bottom-hull-t]]))

; common parts between the two boards.

(defn bottom-hull [c & p]
  (bottom-hull-t (if (get c :configuration-testing-only-inflexible?) 10 0) p))

(defn extra-width
  "extra width between two keys in a row."
  [c]
  (let [nrows (get c :configuration-nrows 5)
        lightcycle? (get c :configuration-lightcycle?)
        default (if lightcycle? 2 (if (> nrows 5) 3.5 2.5))]
    (get c :configuration-extra-width default)))
(defn extra-height
  "extra height between two keys in a column."
  [c]
  (let [lightcycle? (get c :configuration-lightcycle?)
        default (if lightcycle? 0.5 1.0)]
    (get c :configuration-extra-height default)))

(defn keyswitch-height
  "the y dimension of an mx style keyswitch, in millimeter."
  [c]
  (if (get c :configuration-ibnuda-edits?) (if (not= :choc (get c :configuration-switch-type)) 14.0 13.8) 14.4))
(defn keyswitch-width
  "the x dimension of an mx style keyswitch, in millimeter."
  [c]
  (if (get c :configuration-ibnuda-edits?) (if (not= :choc (get c :configuration-switch-type)) 14.0 13.8) 14.4))

(defn keyswitch-width-c [c] (keyswitch-width c))

(def alps-width
  "the x dimension of an alps style keyswitch, in millimeter."
  15.6)
(def alps-notch-width
  15.5)
(def alps-notch-height
  1)
(def alps-height
  "the y dimension of an alps style keyswitch, in millimeter."
  13)

(def sa-profile-key-height 12.7)
(def choc-profile-key-height 3.5)

(defn plate-thickness [c] (if (get c :configuration-ibnuda-edits?) 5 4))

(defn mount-extra [c] (if (get c :configuration-ibnuda-edits?) 3.5 3))
(defn mount-width [c] (+ (keyswitch-width c) (mount-extra c)))
(defn mount-height [c] (+ (keyswitch-height c) (mount-extra c)))

(defn profile-key-height [switch-type] (case switch-type :choc choc-profile-key-height sa-profile-key-height))

(defn cap-top-height [c switch-type]
  (+ (plate-thickness c) (profile-key-height switch-type)))

;;;;;;;;;;;;;;;;;
;; placement function ;;
;;;;;;;;;;;;;;;;;

(defn dm-column-offset
  "Determines how much 'stagger' the columns are for dm.
   0 = inner index finger's column.
   1 = index finger's column.
   2 = middle finger's column.
   3 = ring finger's column.
   4 >= pinky finger's column.
   [x y z] means that it will be staggered by 'x'mm in X axis (left/right),
   'y'mm in Y axis (front/back), and 'z'mm in Z axis (up/down). "
  [c column]
  (let [stagger?       (get c :configuration-stagger?)
        stagger-index  (get c :configuration-stagger-index)
        stagger-middle (get c :configuration-stagger-middle)
        stagger-ring   (get c :configuration-stagger-ring)
        stagger-pinky  (get c :configuration-stagger-pinky)]
    (if stagger?
      (cond (= column 2) stagger-middle
            (= column 3) stagger-ring
            (>= column 4) stagger-pinky
            :else stagger-index)
      (cond (= column 2)  [0   0    -6.5]
            (>= column 4) [0   0     6]
            :else         [0   0     0]))))

(defn fcenterrow
  "Determines where should the center (bottom-most point in the row's curve)
   of the row located at. And most people would want to have the center
   at the homerow. Why does it subtract the value by 3? Because this codebase
   starts the row from the higher row (F row -> num row -> top row)
   and the homerow is number 3 from the last after thumb and bottom row."
  [nrows]
  (let [subtractor (case nrows
                     3 2.5
                     2 2
                     3)]
    (- nrows subtractor)))

(defn flastrow
  "Determines where the last row should be located at."
  [nrows]
  (- nrows 1))
(defn fcornerrow
  "Determines where the penultimate row should be located at."
  [nrows]
  (- nrows 2))
(defn fmiddlerow
  "Should be replaced with `fcenterrow`."
  [nrows]
  (- nrows 3))
(defn flastcol
  "Determines where the last column should be located at. With 0 being inner index
   finger, 1 being index finger, and so on."
  [ncols]
  (- ncols 1))

(defn frow-radius
  "It computes the radius of the row's curve. It takes the value of `pi` divided
   by `alpha` to compute the said radius."
  [c alpha switch-type]
  (+ (/ (/ (+ (mount-height c) (extra-height c)) 2)
        (Math/sin (/ alpha 2)))
     (cap-top-height c switch-type)))

(defn fcolumn-radius
  "It computes the radius of the column's curve. It takes the value of `pi` divided
   by `beta` to compute the said radius."
  [c beta switch-type]
  (+ (/ (/ (+ (mount-width c) (extra-width c)) 2)
        (Math/sin (/ beta 2)))
     (cap-top-height c switch-type)))

; when set `use-wide-pinky?`,
; you will get 1.5u keys for the outermost pinky keys.
(defn offset-for-column
  "This function is used to give additional spacing for the column.
   Main use case is to make the outer pinky keys use 1.5u."
  [c col row]
  (let [use-wide-pinky? (get c :configuration-use-wide-pinky?)
        nrows           (get c :configuration-nrows 5)
        ncols           (get c :configuration-ncols)
        lastrow         (flastrow nrows)
        lastcol         (flastcol ncols)]
    (if (and use-wide-pinky?
             (not= row lastrow)
             (= col lastcol))
      5.5
      0)))

; this is the helper function to 'place' the keys on the defined curve
; of the board.
(defn apply-key-geometry
  "Helps to place the keys in the determined where a key should be placed
   and rotated in xyz coordinate based on its position (row and column).
   It is the implementation detail of `key-place`."
  [c translate-fn rotate-x-fn rotate-y-fn column row shape]
  (let [original-alpha    (get c :configuration-alpha)
        pinky-alpha       (get c :configuration-pinky-alpha original-alpha)
        alpha             (if (>= column 4) pinky-alpha original-alpha)
        beta              (get c :configuration-beta)
        centercol         (get c :configuration-centercol 2)
        centerrow         (fcenterrow (get c :configuration-nrows 5))
        tenting-angle     (get c :configuration-tenting-angle)
        switch-type       (get c :configuration-switch-type)
        keyboard-z-offset (get c :configuration-z-offset)
        web-thickness     (get c :configuration-web-thickness)
        rotate-x-angle    (get c :configuration-rotate-x-angle)
        column-angle      (* beta (- centercol column))
        placed-shape      (->> shape
                               (translate-fn [(offset-for-column c
                                                                 column
                                                                 row)
                                              0
                                              (- (frow-radius c alpha switch-type))])
                               (rotate-x-fn  (* alpha (- centerrow row)))
                               (translate-fn [0 0 (frow-radius c alpha switch-type)])
                               (translate-fn [0 0 (- (fcolumn-radius c beta switch-type))])
                               (rotate-y-fn  column-angle)
                               (translate-fn [0 0 (fcolumn-radius c beta switch-type)])
                               (translate-fn (dm-column-offset c column)))]
    (->> placed-shape
         (rotate-y-fn  tenting-angle)
         (rotate-x-fn  rotate-x-angle)
         (translate-fn [0 0 keyboard-z-offset]))))

(defn rotate-around-x [angle position]
  (mmul
   [[1 0 0]
    [0 (Math/cos angle) (- (Math/sin angle))]
    [0 (Math/sin angle)    (Math/cos angle)]]
   position))

(defn rotate-around-y [angle position]
  (mmul
   [[(Math/cos angle)     0 (Math/sin angle)]
    [0                    1 0]
    [(- (Math/sin angle)) 0 (Math/cos angle)]]
   position))

(def left-wall-x-offset 10)
(def left-wall-z-offset  3)

(defn key-position
  "determines the position of a key based on the
  configuration, column, row, and position.
  it takes configuration to determine whether it is the last column
  and some other options like whether it's a part of a staggered board
  or not."
  [c column row position]
  (apply-key-geometry c
                      (partial map +)
                      rotate-around-x
                      rotate-around-y
                      column
                      row
                      position))

(defn left-key-position
  "determines the position of the left column key position."
  [c row direction]
  (map -
       (key-position c 0 row [(* (mount-width c) -0.5) (* direction (mount-height c) 0.5) 0])
       [left-wall-x-offset 0 left-wall-z-offset]))

(defn index-key-position
  "determines the position of the left column key position."
  [c row direction]
  (map -
       (key-position c 1 row [(* (mount-width c) -0.5) (* direction (mount-height c) 0.5) 0])
       [left-wall-x-offset 0 left-wall-z-offset]))

;;;;;;;;;;;;;;;;;
;; Switch Hole ;;
;;;;;;;;;;;;;;;;;
(defn single-plate
  "Defines the form of switch hole. It determines the whether it uses
   box or mx style based on the `configuration-create-side-nub?`. It also
   asks whether it creates hotswap housing or not based on `configuration-use-hotswap?`.
   and determines whether it should use alps cutout or not based on  `configuration-use-alps?`"
  [c]
  (let [switch-type         (get c :configuration-switch-type)
        create-side-nub?    (case switch-type
                              :mx true
                              :mx-snap-in true
                              false) #_(get c :configuration-create-side-nub?)
      nub-height           (case switch-type
                              :mx-snap-in 0.75
                              0)
        use-alps?           (case switch-type
                              :alps true
                              false) #_(get c :configuration-use-alps?)
        use-choc?           (case switch-type
                              :choc true
                              false)
        use-hotswap?        (get c :configuration-use-hotswap?)
        is-right?           (get c :is-right?)
        plate-projection?   (get c :configuration-plate-projection? false)
        fill-in             (translate [0 0 (/ (plate-thickness c) 2)] (cube alps-width alps-height (plate-thickness c)))
        alps-size           (if (get c :configuration-ibnuda-edits?) 2.7 2.2)
        alps-thickness      (if (get c :configuration-ibnuda-edits?) 2 1.5)
        holder-thickness    (if (get c :configuration-ibnuda-edits?) 1.65 1.5)
        mx-margin           (if (get c :configuration-ibnuda-edits?) 3.3 3)
        top-wall            (case switch-type
                              :alps (->> (cube (+ (keyswitch-width-c c) 3) alps-size (plate-thickness c))
                                         (translate [0
                                                     (+ (/ alps-size 2) (/ alps-height 2))
                                                     (/ (plate-thickness c) 2)]))
                              :choc (->> (cube (+ (keyswitch-width-c c) 3) holder-thickness (* (plate-thickness c) 0.65))
                                         (translate [0
                                                     (+ (/ holder-thickness 2) (/ (keyswitch-height c) 2))
                                                     ( + (/ (plate-thickness c) 2) (/ (- (plate-thickness c) (* (plate-thickness c) 0.65)) 2))]))
                              (->> (cube (+ (keyswitch-width-c c) mx-margin) holder-thickness (plate-thickness c))
                                   (translate [0
                                               (+ (/ holder-thickness 2) (/ (keyswitch-height c) 2))
                                               (/ (plate-thickness c) 2)])))
        left-wall           (case switch-type
                              :alps (union (->> (cube alps-thickness (+ (keyswitch-height c) 3) (plate-thickness c))
                                                (translate [(+ (/ alps-thickness 2) (/ 15.6 2))
                                                            0
                                                            (/ (plate-thickness c) 2)]))
                                           (->> (cube 1.5 (+ (keyswitch-height c) 3) 1.0)
                                                (translate [(+ (/ 1.5 2) (/ alps-notch-width 2))
                                                            0
                                                            (- (plate-thickness c)
                                                               (/ alps-notch-height 2))])))
                              :choc (->> (cube holder-thickness (+ (keyswitch-height c) 3.3) 2.2) ;2.2 is the clamp-height of kailh choc V1 switches according to datasheet 
                                         (translate [(+ (/ holder-thickness 2) (/ (keyswitch-width-c c) 2))
                                                     0
                                                     ( + (/ (plate-thickness c) 2) (/ (- (plate-thickness c) 2.2) 2))]))
                              (->> (cube holder-thickness (+ (keyswitch-height c) mx-margin) (plate-thickness c))
                                   (translate [(+ (/ holder-thickness 2) (/ (keyswitch-width-c c) 2))
                                               0
                                               (/ (plate-thickness c) 2)])))
        side-nub            (->> (binding [*fn* 30] (cylinder 1 2.75))
                                 (rotate (/ Math/PI 2) [1 0 0])
                                 (translate [(+ (/ (keyswitch-width-c c) 2)) 0 (+ 1 nub-height)])
                                 (hull (->> (cube 1.5 2.75 (- (plate-thickness c) nub-height))
                                            (translate [(+ (/ 1.5 2) (/ (keyswitch-width-c c) 2))
                                                        0
                                                        (/ (+ (plate-thickness c) nub-height) 2)]))))
        ; the hole's wall.
        kailh-cutout (->> (cube (/ (keyswitch-width-c c) 3) 1.6 (+ (plate-thickness c) 1.8))
                          (translate [0
                                      (+ (/ 1.5 2) (+ (/ (keyswitch-height c) 2)))
                                      (/ (plate-thickness c))]))
        plate-half          (case switch-type
                              :kailh (union (difference top-wall kailh-cutout) left-wall)
                              (union top-wall
                                     left-wall
                                     (if create-side-nub? (binding [*fn* 100] side-nub))))
        ; the bottom of the hole.
        swap-holder-z-offset (if use-choc? 1.5 -1.5)
        swap-holder         (->> (cube (+ (keyswitch-width-c c) 3) (/ (keyswitch-height c) 2) 3)
                                 (translate [0 (/ (+ (keyswitch-height c) 3) 4) swap-holder-z-offset]))
        ; for the main axis
        main-axis-hole      (->> (cylinder-fn (/ 4.0 2) 10 12))
        plus-hole           (->> (cylinder-fn (/ 3.3 2) 10 8)
                                 (translate (if use-choc? [-5 4 0] [-3.81 2.54 0])))
        minus-hole          (->> (cylinder-fn (/ 3.3 2) 10 8)
                                 (translate (if use-choc? [0 6 5] [2.54 5.08 0])))
        plus-hole-mirrored  (->> (cylinder-fn (/ 3.3 2) 10 8)
                                 (translate (if use-choc? [5 4 5] [3.81 2.54 0])))
        minus-hole-mirrored (->> (cylinder-fn (/ 3.3 2) 10 8)
                                 (translate (if use-choc? [0 6 5] [-2.54 5.08 0])))
        friction-hole       (->> (cylinder-fn (if use-choc? 1 (/ 1.7 2)) 10 8))
        friction-hole-right (translate [(if use-choc? 5.5 5) 0 0] friction-hole)
        friction-hole-left  (translate [(if use-choc? -5.5 -5) 0 0] friction-hole)
        hotswap-base-z-offset (if use-choc? 0.2 -2.6)
        hotswap-base-shape  (->> (cube 19 (if use-choc? 11.5 8.2) 3.5)
                                 (translate [0 5 hotswap-base-z-offset]))
        choc-socket-holder-height 5.5
        choc-socket-holder-thickness 1
        choc-hotswap-socket-holder (difference
                                (->> (cube 10 7 choc-socket-holder-height)
                                     (translate [2 5 hotswap-base-z-offset]))
                                (->> (cube 5 7 choc-socket-holder-height)
                                     (translate [-0.6 6 (+ hotswap-base-z-offset choc-socket-holder-thickness)]))
                                (->> (cube 7 7 choc-socket-holder-height)
                                     (translate [5 4 (+ hotswap-base-z-offset choc-socket-holder-thickness)]))
                                )
        hotswap-holder      (union (if use-choc? choc-hotswap-socket-holder ())
                                (difference swap-holder
                                        main-axis-hole
                                        (union plus-hole plus-hole-mirrored)
                                        (union minus-hole minus-hole-mirrored)
                                        friction-hole-left
                                        friction-hole-right
                                        hotswap-base-shape)
                            )]
    (difference (union plate-half
                       (->> plate-half
                            (mirror [1 0 0])
                            (mirror [0 1 0]))
                       (if plate-projection? fill-in ())
                       (if (and use-hotswap?
                                (not use-alps?))
                         hotswap-holder
                         ())))))
;;;;;;;;;;;;;;;;
;; SA Keycaps ;;
;;;;;;;;;;;;;;;;

(def sa-length 18.25)
(def sa-double-length 37.5)
(defn sa-cap [c]
  (fn [size]
    (case size
      1   (let [bl2     (/ 18.5 2)
                m       (/ 17 2)
                key-cap (hull (->> (polygon [[bl2 bl2] [bl2 (- bl2)] [(- bl2) (- bl2)] [(- bl2) bl2]])
                                   (extrude-linear {:height    0.1
                                                    :twist     0
                                                    :convexity 0})
                                   (translate [0 0 0.05]))
                              (->> (polygon [[m m] [m (- m)] [(- m) (- m)] [(- m) m]])
                                   (extrude-linear {:height    0.1
                                                    :twist     0
                                                    :convexity 0})
                                   (translate [0 0 6]))
                              (->> (polygon [[6 6] [6 -6] [-6 -6] [-6 6]])
                                   (extrude-linear {:height    0.1
                                                    :twist     0
                                                    :convexity 0})
                                   (translate [0 0 12])))]
            (->> key-cap
                 (translate [0 0 (+ 5 (plate-thickness c))])
                 (color [(/ 220 255) (/ 163 255) (/ 163 255) 1])))
      2   (let [bl2     (/ sa-double-length 2)
                bw2     (/ 18.25 2)
                key-cap (hull (->> (polygon [[bw2 bl2] [bw2 (- bl2)] [(- bw2) (- bl2)] [(- bw2) bl2]])
                                   (extrude-linear {:height    0.1
                                                    :twist     0
                                                    :convexity 0})
                                   (translate [0 0 0.05]))
                              (->> (polygon [[6 16] [6 -16] [-6 -16] [-6 16]])
                                   (extrude-linear {:height    0.1
                                                    :twist     0
                                                    :convexity 0})
                                   (translate [0 0 12])))]
            (->> key-cap
                 (translate [0 0 (+ 5 (plate-thickness c))])
                 (color [(/ 127 255) (/ 159 255) (/ 127 255) 1])))
      1.5 (let [bl2     (/ 18.25 2)
                bw2     (/ 28 2)
                key-cap (hull (->> (polygon [[bw2 bl2] [bw2 (- bl2)] [(- bw2) (- bl2)] [(- bw2) bl2]])
                                   (extrude-linear {:height    0.1
                                                    :twist     0
                                                    :convexity 0})
                                   (translate [0 0 0.05]))
                              (->> (polygon [[11 6] [-11 6] [-11 -6] [11 -6]])
                                   (extrude-linear {:height    0.1
                                                    :twist     0
                                                    :convexity 0})
                                   (translate [0 0 12])))]
            (->> key-cap
                 (translate [0 0 (+ 5 (plate-thickness c))])
                 (color [(/ 240 255) (/ 223 255) (/ 175 255) 1])))
      ; Generic keycap size implementation
      (let [bl2     (* size (/ 18.5 2))
            bw2     (/ 18.25 2)
            key-cap (hull (->> (polygon [[bw2 bl2] [bw2 (- bl2)] [(- bw2) (- bl2)] [(- bw2) bl2]])
                               (extrude-linear {:height    0.1
                                                :twist     0
                                                :convexity 0})
                               (translate [0 0 0.05]))
                          (->> (polygon [[6 (- (* 10 size) 4)] [6 (+ (* -10 size) 4)] [-6  (+ (* -10 size) 4)] [-6 (- (* 10 size) 4)]])
                               (extrude-linear {:height    0.1
                                                :twist     0
                                                :convexity 0})
                               (translate [0 0 12])))]
        (->> key-cap
             (translate [0 0 (+ 5 (plate-thickness c))])
             (color [(/ 127 255) (/ 159 255) (/ 127 255) 1]))))))

;; (def web-thickness 7)
(def post-size 0.1)
;; TODO remove the constants once lightcycle has been converted
;; (def web-post
  ;; (->> (cube post-size post-size web-thickness)
       ;; (translate [0 0 (+ (/ web-thickness -2)
                          ;; (plate-thickness c))])))
(defn web-post [c web-thickness]
  (->> (cube post-size post-size web-thickness)
       (translate [0 0 (+ (/ web-thickness -2)
                          (plate-thickness c))])))

(def post-adj (/ post-size 2))

;; TODO remove the constants once lightcycle has been converted
;; (def web-post-tr (translate [(- (/ (mount-width c) 2) post-adj) (- (/ (mount-height c) 2) post-adj) 0] web-post))
;; (def web-post-tl (translate [(+ (/ (mount-width c) -2) post-adj) (- (/ (mount-height c) 2) post-adj) 0] web-post))
;; (def web-post-bl (translate [(+ (/ (mount-width c) -2) post-adj) (+ (/ (mount-height c) -2) post-adj) 0] web-post))
;; (def web-post-br (translate [(- (/ (mount-width c) 2) post-adj) (+ (/ (mount-height c) -2) post-adj) 0] web-post))
(defn web-post-tr [c web-thickness] (translate [(- (/ (mount-width c) 2) post-adj) (- (/ (mount-height c) 2) post-adj) 0] (web-post c web-thickness)))
(defn web-post-tl [c web-thickness] (translate [(+ (/ (mount-width c) -2) post-adj) (- (/ (mount-height c) 2) post-adj) 0] (web-post c web-thickness)))
(defn web-post-bl [c web-thickness] (translate [(+ (/ (mount-width c) -2) post-adj) (+ (/ (mount-height c) -2) post-adj) 0] (web-post c web-thickness)))
(defn web-post-br [c web-thickness] (translate [(- (/ (mount-width c) 2) post-adj) (+ (/ (mount-height c) -2) post-adj) 0] (web-post c web-thickness)))

; length of the first downward-sloping part of the wall (negative)
(def wall-z-offset -15)
; offset in the x and/or y direction for the first downward-sloping part of the wall (negative)
(def wall-xy-offset 5)
; wall thickness parameter; originally 5
;; (def wall-thickness 3)

;; TODO remove those functions once lightcycle has been integrated
;; (defn wall-locate1 [dx dy]
;;   [(* dx wall-thickness) (* dy wall-thickness) -1])
;; (defn wall-locate2 [dx dy]
;;   [(* dx wall-xy-offset) (* dy wall-xy-offset) wall-z-offset])
;; (defn wall-locate3 [dx dy]
;;   [(* dx (+ wall-xy-offset wall-thickness))
;;    (* dy (+ wall-xy-offset wall-thickness))
;;    wall-z-offset])

(defn wall-locate1 [wall-thickness dx dy]
  [(* dx wall-thickness) (* dy wall-thickness) -1])
(defn wall-locate2 [wall-thickness dx dy]
  [(* dx wall-xy-offset) (* dy wall-xy-offset) wall-z-offset])
(defn wall-locate3 [wall-thickness dx dy]
  [(* dx (+ wall-xy-offset wall-thickness))
   (* dy (+ wall-xy-offset wall-thickness))
   wall-z-offset])
;; connectors
(def rj9-cube
  (cube 14.78 13 22.38))
(defn rj9-position
  "determines the position of the rj9 housing.
  it takes a function that generates the coordinate of the housing
  and the configuration."
  [frj9-start c]
  [(first (frj9-start c)) (second (frj9-start c)) 11])
(defn rj9-space
  "puts the space of the rj9 housing based on function and configuration
  that is provided."
  [frj9-start c]
  (translate (rj9-position frj9-start c) rj9-cube))
(defn rj9-holder
  "TODO: doc"
  [frj9-start c]
  (translate
   (rj9-position frj9-start c)
   (difference rj9-cube
               (union (translate [0 2 0] (cube 10.78  9 18.38))
                      (translate [0 0 5] (cube 10.78 13  5))))))

(def usb-holder-size [6.5 13.0 13.6])
(def usb-holder-thickness 4)
(defn usb-holder
  "TODO: doc"
  [fusb-holder-position c]
  (->> (cube (+ (first usb-holder-size) usb-holder-thickness)
             (second usb-holder-size)
             (+ (last usb-holder-size) usb-holder-thickness))
       (translate [(first (fusb-holder-position c))
                   (second (fusb-holder-position c))
                   (/ (+ (last usb-holder-size) usb-holder-thickness) 2)])))
(defn usb-holder-hole
  "TODO: doc"
  [fusb-holder-position c]
  (->> (apply cube usb-holder-size)
       (translate [(first (fusb-holder-position c))
                   (second (fusb-holder-position c))
                   (/ (+ (last usb-holder-size) usb-holder-thickness) 2)])))

(defn screw-insert-shape [bottom-radius top-radius height]
  (union (cylinder [bottom-radius top-radius] height)
         (translate [0 0 (/ height 2)] (sphere top-radius))))

(def screw-insert-height 3.8)
(def screw-insert-bottom-radius (/ 5.31 2))
(def screw-insert-top-radius (/ 5.1 2))

(defn screw-insert-holes
  "TODO: doc.
   but basically it takes a function that places screw holes with the following specs."
  [placement-function c]
  (placement-function c
                      screw-insert-bottom-radius
                      screw-insert-top-radius
                      screw-insert-height))
(defn screw-insert-outers
  "TODO: doc.
   but basically it takes a function that places outer parts of screw holes with the following specs."
  [placement-function c]
  (placement-function c
                      (+ screw-insert-bottom-radius 1.6)
                      (+ screw-insert-top-radius 1.6)
                      (+ screw-insert-height 1.5)))
(defn screw-insert-screw-holes
  "TODO: doc."
  [placement-function c]
  (placement-function c 1.7 1.7 350))

(defn screw-insert [c column row bottom-radius top-radius height]
  (let [lastcol     (flastcol (get c :configuration-ncols))
        lastrow     (flastrow (get c :configuration-nrows 5))
        shift-right (= column lastcol)
        shift-left  (= column 0)
        shift-up    (and (not (or shift-right shift-left)) (= row 0))
        shift-down  (and (not (or shift-right shift-left)) (>= row lastrow))
        wall-thickness (get c :configuration-wall-thickness 5)
        position    (if shift-up
                      (key-position c column row (map + (wall-locate2 wall-thickness 0  1) [0 (/ (mount-height c) 2) 0]))
                      (if shift-down
                        (key-position c column row (map - (wall-locate2 wall-thickness 0 -1) [0 (/ (mount-height c) 2) 0]))
                        (if shift-left
                          (map + (left-key-position c row 0) (wall-locate3 wall-thickness -1 0))
                          (key-position c column row (map + (wall-locate2 wall-thickness 1  0) [(/ (mount-width c) 2) 0 0])))))]
    (->> (screw-insert-shape bottom-radius top-radius height)
         (translate [(first position) (second position) (/ height 2)]))))
