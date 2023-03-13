(ns dactyl-generator.lightcycle
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.model :refer [translate cube hull rotate cylinder union mirror extrude-linear polygon sphere difference color sphere-fn cylinder-fn project cut *fn*]]
            [dactyl-generator.util :refer [bottom triangle-hulls deg2rad]]
            [dactyl-generator.common
             :as cmn :refer [screw-insert screw-insert-holes screw-insert-screw-holes screw-insert-outers
                             single-plate sa-cap sa-length sa-double-length mount-width mount-height plate-thickness
                             wall-locate1 wall-locate2 wall-locate3 key-position apply-key-geometry
                             web-post post-adj flastrow flastcol fmiddlerow fcenterrow  fcornerrow
                             sa-profile-key-height bottom-hull
                             ]]))

(def pi Math/PI)

;;;;;;;;;;;;;;;;;;;;;;;;;
;; Placement Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;;;

(defn frows [c]
  (let [use-numrow? (get c :configuration-use-numrow?)
        use-lastrow? (get c :configuration-use-lastrow?)
        row-start (if use-numrow? 0 1)
        row-end (if use-lastrow? 5 4)]
    (range row-start row-end)))

(defn flastrow-lightcycle [use-lastrow?]
  (if use-lastrow? 5 4))
(defn fcornerrow-lightcycle [use-lastrow?]
  (if use-lastrow? 4 3))
(defn fmiddlerow-lightcycle [use-lastrow?]
  (if use-lastrow? 3 2))

(defn fpenultcol [ncols] (dec ncols))
(defn fantecol   [ncols] (dec (fpenultcol ncols)))

(defn fthumb-offset [c]
  (let [thumb-offset-x (get c :configuration-thumb-offset-x)
        thumb-offset-y (get c :configuration-thumb-offset-y)
        thumb-offset-z (get c :configuration-thumb-offset-z)]
    [thumb-offset-x thumb-offset-y (+ thumb-offset-z 13)]))

(defn column-offset [column]
  (cond
    (= column 2) [0 2.82 -3.0] ;;was moved -4.5
    (>= column 4) [0 -5.8 5.64]
    :else [0 0 0]))

(defn manuform-column-offset [column]
  (cond
    (= column 2) [0 2.82 -4.5]
    (>= column 4) [0 -12 5.64]            ; original [0 -5.8 5.64]
    :else [0 0 0]))

(defn wide-pinky [c column row]
  (let [use-wide-pinky? (get c :configuration-use-wide-pinky?)
        ncols           (get c :configuration-ncols)
        columns         (range 0 ncols)
        last-column     (last columns)]
    (if (and use-wide-pinky?
             (= last-column column)
             (not= 4 row))
      5.5
      0)))

(defn key-place [c column row shape]
  (let [alpha            (get c :configuration-alpha)
        beta             (get c :configuration-beta)
        tenting-angle    (get c :configuration-tenting-angle)
        z-offset         (get c :configuration-z-offset)
        switch-type      (get c :configuration-switch-type)
        manuform-offset? (get c :configuration-manuform-offset? false)
        offset           (if manuform-offset?
                           (manuform-column-offset column)
                           (column-offset column))
        column-angle     (* beta (- 2 column))
        placed-shape     (->> shape
                              (translate [(wide-pinky c column row) 0 (- (cmn/frow-radius c alpha switch-type))])
                              (rotate (* alpha (- 2 row)) [1 0 0])
                              (translate [0 0 (cmn/frow-radius c alpha switch-type)])
                              (translate [0 0 (- (cmn/fcolumn-radius c beta switch-type))])
                              (rotate column-angle [0 1 0])
                              (translate [0 0 (cmn/fcolumn-radius c beta switch-type)])
                              (translate offset))]
    (->> placed-shape
         (rotate tenting-angle [0 1 0])
         (translate [0 0 z-offset]))))

(defn case-place [c column row shape]
  (let [alpha            (get c :configuration-alpha)
        beta             (get c :configuration-beta)
        tenting-angle    (get c :configuration-tenting-angle)
        z-offset         (get c :configuration-z-offset)

        manuform-offset? (get c :configuration-manuform-offset? false)
        column-offset    (if (and manuform-offset?
                                  (> row 2))
                           [0 -10.35 8.64]
                           [0 -4.35 8.64])
        switch-type      (get c :configuration-switch-type)
        column-angle     (* beta (- 2 column))
        placed-shape     (->> shape
                              (translate [0 0 (- (cmn/frow-radius c alpha switch-type))])
                              (rotate (* alpha (- 2 row)) [1 0 0])
                              (translate [0 0 (cmn/frow-radius c alpha switch-type)])
                              (translate [0 0 (- (cmn/fcolumn-radius c beta switch-type))])
                              (rotate column-angle [0 1 0])
                              (translate [0 0 (cmn/fcolumn-radius c beta switch-type)])
                              (translate column-offset))]
    (->> placed-shape
         (rotate tenting-angle [0 1 0])
         (translate [0 0 z-offset]))))

(defn key-holes [c]
  (let [ncols                (get c :configuration-ncols)
        use-alps?            (get c :configuration-use-alps?)
        use-lastrow?         (get c :configuration-use-lastrow?)
        hide-last-pinky?     (get c :configuration-hide-last-pinky?)
        rotation-for-keyhole (if use-alps? 0 0)
        columns              (range 0 ncols)
        rows                 (frows c)
        last-pinky-location  (fn [column row]
                               (and (= row 4)
                                    (> (last columns) 4)
                                    (= column (last columns))))
        hide-pinky           (fn [column row]
                               (not (and use-lastrow?
                                         hide-last-pinky?
                                         (last-pinky-location column row))))]
    (apply union
           (for [column columns
                 row    rows
                 :when  (not (and (= column 0) (> row 3)))
                 :when  (hide-pinky column row)]
             (->> (color [1 1 0] (single-plate c))
                  (rotate (deg2rad rotation-for-keyhole) [0 0 1])
                  (key-place c column row))))))

(defn caps [c]
  (let [ncols               (get c :configuration-ncols)
        use-lastrow?        (get c :configuration-use-lastrow?)
        use-wide-pinky?     (get c :configuration-use-wide-pinky?)
        hide-last-pinky?    (get c :configuration-hide-last-pinky?)
        columns             (range 0 ncols)
        rows                (frows c)
        lastrow             (flastrow-lightcycle (get c :configuration-use-lastrow?))
        last-pinky-location (fn [column row]
                              (and (= row 4)
                                   (> (last columns) 4)
                                   (= column (last columns))))
        hide-pinky          (fn [column row]
                              (not (and use-lastrow?
                                        hide-last-pinky?
                                        (last-pinky-location column row))))
        sa-cap-unit         (fn [column row]
                              (if (and use-wide-pinky?
                                       (= (last columns) column)
                                       (not= 4 row))
                                1.5
                                1))]
    (apply union
           (for [column columns
                 row    rows
                 :when  (or (not= column 0)
                            (not= row 4))
                 :when  (hide-pinky column row)]
             (->> ((sa-cap c) (sa-cap-unit column row))
                  (key-place c column row))))))

;;;;;;;;;;;;;;;;;;;;
;; Web Connectors ;;
;;;;;;;;;;;;;;;;;;;;

(defn connectors [c]
  (let [use-lastrow?        (get c :configuration-use-lastrow?)
        ncols               (get c :configuration-ncols)
        hide-last-pinky?    (get c :configuration-hide-last-pinky?)
        columns             (range 0 ncols)
        rows                (frows c)
        lastrow             (flastrow-lightcycle use-lastrow?)
        cornerrow           (fcornerrow-lightcycle use-lastrow?)
        web-thickness       (get c :configuration-web-thickness)
        last-pinky-location (fn [column row]
                              (and (= row 4)
                                   (> (last columns) 4)
                                   (= column (last columns))))
        hide-pinky          (fn [column row]
                              (not (and use-lastrow?
                                        hide-last-pinky?
                                        (last-pinky-location column row))))]
    (apply union
           (if-not (or (not (> (last columns) 4))
                       (hide-pinky (last columns) cornerrow))
             (triangle-hulls (key-place c (last columns) cornerrow (cmn/web-post-tr c web-thickness))
                             (key-place c (last columns) cornerrow (cmn/web-post-tl c web-thickness))
                             (key-place c (last columns) cornerrow (cmn/web-post-br c web-thickness))
                             (key-place c (last columns) cornerrow (cmn/web-post-bl c web-thickness)))
             ())
           (concat
          ;; Row connections
            (for [column (drop-last columns)
                  row    rows
                  :when  (or (not= column 0)
                             (and (= column 0)
                                  (< row (if use-lastrow? cornerrow lastrow))))]
              (triangle-hulls
               (key-place c (inc column) row (cmn/web-post-tl c web-thickness))
               (key-place c column row (cmn/web-post-tr c web-thickness))
               (key-place c (inc column) row (cmn/web-post-bl c web-thickness))
               (key-place c column row (cmn/web-post-br c web-thickness))))

          ;; Column connections
            (for [column columns
                  row    (drop-last rows)
                  :when  (or (not= column 0)
                             (not (and (= column 0)
                                       (> row 2))))]
              (triangle-hulls
               (key-place c column row (cmn/web-post-bl c web-thickness))
               (key-place c column row (cmn/web-post-br c web-thickness))
               (key-place c column (inc row) (cmn/web-post-tl c web-thickness))
               (key-place c column (inc row) (cmn/web-post-tr c web-thickness))))

          ;; Diagonal connections
            (for [column (drop-last columns)
                  row    (drop-last rows)
                  :when  (not (and (= column 0)
                                   (> row cornerrow)))]
              (triangle-hulls
               (key-place c column row (cmn/web-post-br c web-thickness))
               (key-place c column (inc row) (cmn/web-post-tr c web-thickness))
               (key-place c (inc column) row (cmn/web-post-bl c web-thickness))
               (key-place c (inc column) (inc row) (cmn/web-post-tl c web-thickness))))))))

;;;;;;;;;;;;
;; Thumbs ;;
;;;;;;;;;;;;

(defn thumb-place [c column row shape]
  (let [thumb-alpha         (get c :configuration-thumb-alpha)
        thumb-beta          (get c :configuration-thumb-beta)
        thumb-tenting-angle (get c :configuration-thumb-tenting-angle)
        rotation-angle      (if (neg? thumb-tenting-angle) [0 1 0] [1 1 0])
        thumb-offset        (fthumb-offset c)
        cap-top-height      (+ (plate-thickness c) sa-profile-key-height)
        row-radius          (+ (/ (/ (+ (mount-height c) 1) 2)
                                  (Math/sin (/ thumb-alpha 2)))
                               cap-top-height)
        column-radius       (+ (/ (/ (+ (mount-width c) 2) 2)
                                  (Math/sin (/ thumb-beta 2)))
                               cap-top-height)]
    (->> shape
         (translate [0 0 (- row-radius)])
         (rotate (* thumb-alpha row) [1 0 0])
         (translate [0 0 row-radius])
         (translate [0 0 (- column-radius)])
         (rotate (* column thumb-beta) [0 1 0])
         (translate [0 0 column-radius])
         (translate [(mount-width c) 0 0])
         (rotate (* pi (- (/ 1 4) (/ 3 16))) [0 0 1])
         #_(rotate beta [1 1 0])
         #_(rotate thumb-beta [1 1 0])
         (rotate thumb-tenting-angle rotation-angle)
         (translate thumb-offset))))

(defn thumb-2x-column [c shape]
  (thumb-place c 0 -0.5 (rotate (/ pi 1) [0 0 1] shape)))

(defn thumb-2x+1-column [c shape]
  (union (thumb-place c 1 -0.5 (rotate (/ pi 2) [0 0 1] shape))
         (thumb-place c 1 1 shape)))

(defn thumb-1x-column [c shape]
  (union (thumb-place c 2 (/ -3 4) shape)
         (thumb-place c 2  (/ 3 4) shape)))

(defn extended-plate-height [c size] (/ (- (* (+ 1 sa-length) size) (mount-height c)) 2))

(defn double-plates [c]
  (let [plate-height      (extended-plate-height c 2)
        web-thickness (get c :configuration-web-thickness)
        top-plate         (->> (cube (mount-width c) plate-height web-thickness)
                               (translate [0 (/ (+ plate-height (mount-height c)) 2)
                                           (- (plate-thickness c) (/ web-thickness 2))]))
        stabilizer-cutout (union (->> (cube 14.2 3.5 web-thickness)
                                      (translate [0.5 12 (- (plate-thickness c) (/ web-thickness 2))])
                                      (color [1 0 0 0.5]))
                                 (->> (cube 16 3.5 web-thickness)
                                      (translate [0.5 12 (- (plate-thickness c) (/ web-thickness 2) 1.4)])
                                      (color [1 0 0 0.5])))
        top-plate         (difference top-plate stabilizer-cutout)]
    (color [1 0 0] (union top-plate (mirror [0 1 0] top-plate)))))

(defn extended-plates [c size]
  (let [plate-height (extended-plate-height c size)
        web-thickness (get c :configuration-web-thickness)
        top-plate    (->> (cube (mount-width c) plate-height web-thickness)
                          (translate [0 (/ (+ plate-height (mount-height c)) 2)
                                      (- (plate-thickness c) (/ web-thickness 2))]))]
    (color [0 1 1] (union top-plate (mirror [0 1 0] top-plate)))))

(defn thumb-layout [c shape]
  (let [thumb-count (get c :configuration-thumb-count)]
    (union
     (case thumb-count
       :eight (union (thumb-place c 0 -1 (union shape (extended-plates c 1)))
                     (thumb-place c 0  0 (union shape (extended-plates c 1)))
                     (thumb-place c 1 -1 (union shape (extended-plates c 1)))
                     (thumb-place c 1  0 (union shape (extended-plates c 1))))
       (union (thumb-place c 0 -0.5 (union shape (extended-plates c 2)))
              (thumb-place c 1 -0.5 (union shape (extended-plates c 2)))))
     (case thumb-count
       :two ()
       :three (thumb-place c 1    1 (union shape (extended-plates c 1)))
       :five (union (thumb-place c 1    1 (union shape (extended-plates c 1)))
                    (thumb-place c 2 (/ -3 4) (union shape (extended-plates c 1.5)))
                    (thumb-place c 2  (/ 3 4) (union shape (extended-plates c 1.5))))
       (union (thumb-place c 1  1 (union shape (extended-plates c 1)))
              (thumb-place c 2  1 (union shape (extended-plates c 1)))
              (thumb-place c 2  0 (union shape (extended-plates c 1)))
              (thumb-place c 2 -1 (union shape (extended-plates c 1))))))))

(defn thumbcaps [c]
  (let [thumb-count (get c :configuration-thumb-count)]
    (union
     (case thumb-count
       :eight (union (thumb-place c 0 -1 ((sa-cap c) 1))
                     (thumb-place c 0  0 ((sa-cap c) 1))
                     (thumb-place c 1 -1 ((sa-cap c) 1))
                     (thumb-place c 1  0 ((sa-cap c) 1)))
       (union (thumb-2x-column c ((sa-cap c) 2))
              (thumb-place c 1 -0.5 ((sa-cap c) 2))))
     (case thumb-count
       :two ()
       :three (thumb-place c 1 1 ((sa-cap c) 1))
       :five (union (thumb-1x-column c (rotate (/ pi 2) [0 0 1] ((sa-cap c) 1.5)))
                    (thumb-place c 1 1 ((sa-cap c) 1)))
       (union (thumb-place c 1  1 ((sa-cap c) 1))
              (thumb-place c 2  1 ((sa-cap c) 1))
              (thumb-place c 2  0 ((sa-cap c) 1))
              (thumb-place c 2 -1 ((sa-cap c) 1)))))))

(defn thumb-connectors [c]
  (let [thumb-count  (get c :configuration-thumb-count)
        use-lastrow? (get c :configuration-use-lastrow?)
        cornerrow    (fcornerrow-lightcycle use-lastrow?)
        web-thickness (get c :configuration-web-thickness)
        thumb-tl     #(->> (cmn/web-post-tl c web-thickness)
                           (translate [0 (extended-plate-height c %) 0]))
        thumb-bl     #(->> (cmn/web-post-bl c web-thickness)
                           (translate [0 (- (extended-plate-height c %)) 0]))
        thumb-tr     #(->> (cmn/web-post-tr c web-thickness)
                           (translate [0 (extended-plate-height c %) 0]))
        thumb-br     #(->> (cmn/web-post-br c web-thickness)
                           (translate [0 (- (extended-plate-height c %)) 0]))]
    ;;Connecting main thumb keys.
    (union
     (case thumb-count
       :eight (union
               (triangle-hulls (thumb-place c 0  0  (thumb-bl 1))
                               (thumb-place c 1  0  (thumb-br 1))
                               (thumb-place c 0  0  (thumb-tl 1))
                               (thumb-place c 1  0  (thumb-tr 1)))
               (triangle-hulls (thumb-place c 0 -1 (thumb-bl 1))
                               (thumb-place c 1 -1 (thumb-br 1))
                               (thumb-place c 0 -1 (thumb-tl 1))
                               (thumb-place c 1 -1 (thumb-tr 1)))
               (triangle-hulls (thumb-place c 0 -1 (thumb-tl 1))
                               (thumb-place c 0 -1 (thumb-tr 1))
                               (thumb-place c 0  0 (thumb-bl 1))
                               (thumb-place c 0  0 (thumb-br 1)))
               (triangle-hulls (thumb-place c 1 -1 (thumb-tl 1))
                               (thumb-place c 1 -1 (thumb-tr 1))
                               (thumb-place c 1  0 (thumb-bl 1))
                               (thumb-place c 1  0 (thumb-br 1)))
               (triangle-hulls (thumb-place c 0 -1 (thumb-tl 1))
                               (thumb-place c 1 -1 (thumb-tr 1))
                               (thumb-place c 0  0 (thumb-bl 1))
                               (thumb-place c 1  0 (thumb-br 1))))
       (triangle-hulls #_(thumb-place c 1 -0.5 (thumb-tl 2))
                       (thumb-place c 0 -0.5 (thumb-bl 2))
                       (thumb-place c 1 -0.5 (thumb-br 2))
                       (thumb-place c 0 -0.5 (thumb-tl 2))
                       (thumb-place c 1 -0.5 (thumb-tr 2))
                       #_(thumb-place c 1  1   (thumb-br 1))))

     (case thumb-count
       :eight (union
               (triangle-hulls (thumb-place c 1  0 (thumb-bl 1))
                               (thumb-place c 2  0 (thumb-br 1))
                               (thumb-place c 1  0 (thumb-tl 1))
                               (thumb-place c 2  0 (thumb-tr 1)))
               (triangle-hulls (thumb-place c 1 -1 (thumb-bl 1))
                               (thumb-place c 2 -1 (thumb-br 1))
                               (thumb-place c 1 -1 (thumb-tl 1))
                               (thumb-place c 2 -1 (thumb-tr 1)))
               (triangle-hulls (thumb-place c 1  1 (thumb-bl 1))
                               (thumb-place c 2  1 (thumb-br 1))
                               (thumb-place c 1  1 (thumb-tl 1))
                               (thumb-place c 2  1 (thumb-tr 1)))
               (triangle-hulls (thumb-place c 2 -1 (thumb-tl 1))
                               (thumb-place c 2 -1 (thumb-tr 1))
                               (thumb-place c 2  0 (thumb-bl 1))
                               (thumb-place c 2  0 (thumb-br 1)))
               (triangle-hulls (thumb-place c 1  0 (thumb-tl 1))
                               (thumb-place c 1  0 (thumb-tr 1))
                               (thumb-place c 1  1 (thumb-bl 1))
                               (thumb-place c 1  1 (thumb-br 1)))
               (triangle-hulls (thumb-place c 2  0 (thumb-tl 1))
                               (thumb-place c 2  0 (thumb-tr 1))
                               (thumb-place c 2  1 (thumb-bl 1))
                               (thumb-place c 2  1 (thumb-br 1)))
               (triangle-hulls (thumb-place c 1 -1 (thumb-tl 1))
                               (thumb-place c 2 -1 (thumb-tr 1))
                               (thumb-place c 1  0 (thumb-bl 1))
                               (thumb-place c 2  0 (thumb-br 1)))
               (triangle-hulls (thumb-place c 1  0 (thumb-tl 1))
                               (thumb-place c 2  0 (thumb-tr 1))
                               (thumb-place c 1  1 (thumb-bl 1))
                               (thumb-place c 2  1 (thumb-br 1))))
       :six (union
             (triangle-hulls (thumb-place c 1  1   (thumb-br 1))
                             (thumb-place c 1  1   (thumb-bl 1))
                             (thumb-place c 1 -0.5 (thumb-tr 2))
                             (thumb-place c 1 -0.5 (thumb-tl 2)))
             (triangle-hulls (thumb-place c 2  1   (thumb-tr 1))
                             (thumb-place c 1  1   (thumb-tl 1))
                             (thumb-place c 2  1   (thumb-br 1))
                             (thumb-place c 1  1   (thumb-bl 1))
                             (thumb-place c 1 -0.5 (thumb-tl 2))
                             (thumb-place c 2  1   (thumb-br 1))
                             (thumb-place c 2  0   (thumb-tr 1))
                             (thumb-place c 2  1   (thumb-bl 1))
                             (thumb-place c 2  0   (thumb-tl 1)))
             (triangle-hulls (thumb-place c 2  0   (thumb-tr 1))
                             (thumb-place c 1 -0.5 (thumb-tl 2))
                             (thumb-place c 2  0   (thumb-br 1))
                             (thumb-place c 1 -0.5 (thumb-bl 2))
                             (thumb-place c 2 -1   (cmn/web-post-br c web-thickness)))
             (triangle-hulls (thumb-place c 2  0   (thumb-bl 1))
                             (thumb-place c 2  0   (thumb-br 1))
                             (thumb-place c 2 -1   (cmn/web-post-tl c web-thickness))
                             (thumb-place c 2 -1   (cmn/web-post-tr c web-thickness))))
       :five (union
              (triangle-hulls (thumb-place c 1  1   (thumb-br 1))
                              (thumb-place c 1  1   (thumb-bl 1))
                              (thumb-place c 1 -0.5 (thumb-tr 2))
                              (thumb-place c 1 -0.5 (thumb-tl 2)))
              (triangle-hulls (thumb-place c 2  (/ 3 4) (thumb-br 1.5))
                              (thumb-place c 2  (/ 3 4) (thumb-bl 1.5))
                              (thumb-place c 2 (/ -3 4) (thumb-tr 1.5))
                              (thumb-place c 2 (/ -3 4) (thumb-tl 1.5)))
              (triangle-hulls (thumb-place c 2  (/ 3 4) (thumb-br 1.5))
                              (thumb-place c 2  (/ 3 4) (thumb-bl 1.5))
                              (thumb-place c 2 (/ -3 4) (thumb-tr 1.5))
                              (thumb-place c 2 (/ -3 4) (thumb-tl 1.5)))
              (triangle-hulls (thumb-place c 2 (/ -3 4) (thumb-br 1.5))
                              (thumb-place c 1 -0.5 (thumb-bl 2))
                              (thumb-place c 2 (/ -3 4) (thumb-tr 1.5))
                              (thumb-place c 1 -0.5 (thumb-tl 2))
                              (thumb-place c 2  (/ 3 4) (thumb-br 1.5))
                              (thumb-place c 1  1   (thumb-bl 1))
                              (thumb-place c 2  (/ 3 4) (thumb-tr 1.5))
                              (thumb-place c 1  (/ 7 8) (thumb-tl 1.25))))
       :three (triangle-hulls (thumb-place c 1  1   (thumb-br 1))
                              (thumb-place c 1  1   (thumb-bl 1))
                              (thumb-place c 1 -0.5 (thumb-tr 2))
                              (thumb-place c 1 -0.5 (thumb-tl 2)))
       ())

      ;;Connecting the thumb to everything
     (case thumb-count
       :two (triangle-hulls (thumb-place c 0 -0.5 (thumb-br 2))
                            (key-place   c 1 cornerrow (cmn/web-post-bl c web-thickness))
                            (thumb-place c 0 -0.5 (thumb-tr 2))
                            (key-place   c 1    3 (cmn/web-post-bl c web-thickness))
                            (thumb-place c 0 -0.5 (thumb-tr 2))
                            (key-place   c 0    3 (cmn/web-post-br c web-thickness))
                            (key-place   c 0    3 (cmn/web-post-bl c web-thickness))
                            (thumb-place c 0 -0.5 (thumb-tr 2))
                            (thumb-place c 0 -0.5 (thumb-tl 2))
                            (key-place   c 0    3 (cmn/web-post-bl c web-thickness))
                            (thumb-place c 1 -0.5 (thumb-tr 2))
                            (key-place   c 0    3 (cmn/web-post-bl c web-thickness)))
       :eight (triangle-hulls (thumb-place c 0 -1        (thumb-br 1))
                              (key-place   c 1 cornerrow (cmn/web-post-bl c web-thickness))
                              (thumb-place c 0 -1        (thumb-tr 1))
                              (thumb-place c 0  0        (thumb-br 1))
                              (key-place   c 1 cornerrow (cmn/web-post-bl c web-thickness))
                              (thumb-place c 0  0        (thumb-tr 1))
                              (key-place   c 1  3        (cmn/web-post-bl c web-thickness))
                              (key-place   c 1  3        (cmn/web-post-bl c web-thickness))
                              (thumb-place c 0  0        (thumb-tr 1))
                              (key-place   c 0  3        (cmn/web-post-br c web-thickness))
                              (key-place   c 0  3        (cmn/web-post-bl c web-thickness))
                              (thumb-place c 0  0        (thumb-tr 1))
                              (thumb-place c 0  0        (thumb-tl 1))
                              (key-place   c 0  3        (cmn/web-post-bl c web-thickness))
                              (thumb-place c 1  0        (thumb-tr 1))
                              (thumb-place c 1  1        (thumb-br 1))
                              (key-place   c 0  3        (cmn/web-post-bl c web-thickness))
                              (thumb-place c 1  1        (thumb-tr 1))
                              (key-place   c 0  3        (cmn/web-post-tl c web-thickness))
                              #_(thumb-place c 0  0        (thumb-tl 1))
                              #_(thumb-place c 1  0        (thumb-tr 1))
                              #_(key-place   c 0  3        (cmn/web-post-bl c web-thickness))
                              #_(key-place   c 0  3        (cmn/web-post-tl c web-thickness))
                              #_(thumb-place c 1  0        (thumb-tr 1))
                              #_(thumb-place c 1  1        (thumb-tr 1)))
       (triangle-hulls (thumb-place c 0 -0.5      (thumb-br 2))
                       (key-place   c 1 cornerrow (cmn/web-post-bl c web-thickness))
                       (thumb-place c 0 -0.5      (thumb-tr 2))
                       (key-place   c 1  4        (cmn/web-post-tl c web-thickness))
                       (key-place   c 1  3        (cmn/web-post-bl c web-thickness))
                       (thumb-place c 0 -0.5      (thumb-tr 2))
                       (key-place   c 0  3        (cmn/web-post-br c web-thickness))
                       (key-place   c 0  3        (cmn/web-post-bl c web-thickness))
                       (thumb-place c 0 -0.5      (thumb-tr 2))
                       (thumb-place c 0 -0.5      (thumb-tl 2))
                       (key-place   c 0  3        (cmn/web-post-bl c web-thickness))
                       (thumb-place c 1 -0.5      (thumb-tr 2))
                       (thumb-place c 1  1        (thumb-br 1))
                       (key-place   c 0  3        (cmn/web-post-bl c web-thickness))
                       (key-place   c 0  3        (cmn/web-post-tl c web-thickness))
                       (thumb-place c 1  1        (thumb-br 1))
                       (thumb-place c 1  1        (thumb-tr 1)))))))

(defn thumb [c]
  (let [thumb-count (get c :configuration-thumb-count)]
    (union
     (thumb-layout c (rotate (/ Math/PI 2) [0 0 1] (single-plate c)))
     (color [1 0 0] (thumb-connectors c))

     #_(case thumb-count
         :five (union
                (thumb-place c 0 -0.5 (extended-plates c 2))
                (thumb-place c 1 -0.5 (extended-plates c 2)))
         :three (thumb-place c 1   1  (extended-plates c 1))
         ()))))

;;;;;;;;;;
;; Case ;;
;;;;;;;;;;

;; In column units
(defn right-wall-column [c]
  (let [lastcol         (- (get c :configuration-ncols) 1)
        use-wide-pinky? (get c :configuration-use-wide-pinky?)]
    (if use-wide-pinky?
      (+ lastcol 1.05)
      (+ lastcol 0.55))))
(def left-wall-column (/ -8 15))
(defn thumb-back-y [c]
  (let [thumb-count (get c :configuration-thumb-count)]
    (case thumb-count :two -0.07 0.93)))
(def thumb-right-wall (- -0.5 0.05))
(def thumb-front-row (+ -1 0.07))
(defn thumb-left-wall-column [c]
  (let [thumb-count (get c :configuration-thumb-count)
        thumb-column (case thumb-count
                       :five (/ 5 2)
                       :six (/ 5 2)
                       :eight (/ 5 2)
                       (/ 3 2))]
    (+ thumb-column 0.05)))
(defn back-y [c]
  (let [rows (frows c)]
    (+ (first rows) #_0.02 -0.15)))

(defn range-inclusive [start end step]
  (concat (range start end step) [end]))

(def wall-step 0.1)
(def wall-sphere-n 20) ;;Sphere resolution, lower for faster renders

(defn wall-sphere-at [thick-wall? coords]
  (let [thickness   (if thick-wall? 2 1)]
    (->> (sphere-fn thickness wall-sphere-n)
         (translate coords))))

(defn scale-to-range [start end x]
  (+ start (* (- end start) x)))

(defn wall-sphere-bottom [c thick-wall? front-to-back-scale]
  (wall-sphere-at thick-wall?
                  [0
                   (scale-to-range
                    (+ (/ (mount-height c) -2) -3.5)
                    (+ (/ (mount-height c) 2) 5.0)
                    front-to-back-scale)
                   -5]))

(defn wall-sphere-top [c thick-wall? front-to-back-scale use-border?]
  (wall-sphere-at thick-wall?
                  [0
                   (scale-to-range
                    (+ (/ (mount-height c) -2) -3.5)
                    (+ (/ (mount-height c) 2) 3.5)
                    front-to-back-scale)
                   (if use-border? 5 0)]))

(defn wall-sphere-top-back [c thick-wall? use-border?]
  (wall-sphere-top c thick-wall? 1 use-border?))
(defn wall-sphere-bottom-back [c thick-wall?]
  (wall-sphere-bottom c thick-wall? 1))
(defn wall-sphere-bottom-front [c thick-wall?]
  (wall-sphere-bottom c thick-wall? 0))
(defn wall-sphere-top-front [c thick-wall? use-border?]
  (wall-sphere-top c thick-wall? 0 use-border?))

(defn top-case-cover [place-fn sphere
                      x-start x-end
                      y-start y-end
                      step]
  (apply union
         (for [x (range-inclusive x-start (- x-end step) step)
               y (range-inclusive y-start (- y-end step) step)]
           (hull (place-fn x y sphere)
                 (place-fn (+ x step) y sphere)
                 (place-fn x (+ y step) sphere)
                 (place-fn (+ x step) (+ y step) sphere)))))

(defn front-wall [c]
  (let [use-lastrow?                   (get c :configuration-use-lastrow?)
        ncols                          (get c :configuration-ncols)
        lastrow                        (flastrow-lightcycle use-lastrow?)
        manuform-offset?               (get c :configuration-manuform-offset?)
        use-border?                    (get c :configuration-use-border?)
        cornerrow                      (fcornerrow-lightcycle use-lastrow?)
        thick-wall?                    (get c :configuration-thick-wall?)
        hide-last-pinky?               (get c :configuration-hide-last-pinky?)
        use-wide-pinky?                (get c :configuration-use-wide-pinky?)
        web-thickness (get c :configuration-web-thickness)
        penultcol                      (fpenultcol ncols)
        antecol                        (fantecol ncols)
        step                           wall-step ;;0.1
        wall-step                      0.1 ;;0.05
        place                          (partial case-place c)
        top-cover                      (fn [x-start x-end y-start y-end]
                                         (top-case-cover place (wall-sphere-top-front c thick-wall? use-border?)
                                                         x-start x-end y-start y-end
                                                         wall-step))
        index-finger-cover-multiplier  (if manuform-offset? 0.85 0.92)
        middle-finger-cover-multiplier (if manuform-offset? 0.80 0.88)
        ring-finger-cover-multiplier   (if manuform-offset? 0.85 0.92)
        pinky-finger-cover-multiplier  (if manuform-offset? 0.75 0.75)]
    (union
     (apply union
            (for [x (range-inclusive 0.7 (- (right-wall-column c) step) step)]
              (hull (place x cornerrow (wall-sphere-top-front c thick-wall? use-border?))
                    (place (+ x step) cornerrow (wall-sphere-top-front c thick-wall? use-border?))
                    (place x cornerrow (wall-sphere-bottom-front c thick-wall?))
                    (place (+ x step) cornerrow (wall-sphere-bottom-front c thick-wall?)))))
     (apply union
            (for [x (range-inclusive 0.7 (- (right-wall-column c) step) step)]
              (bottom-hull c (place x cornerrow (wall-sphere-bottom-front c thick-wall?))
                           (place (+ x step) cornerrow (wall-sphere-bottom-front c thick-wall?)))))
     (apply union
            (for [x (range-inclusive 0.5 0.7 0.1)]
              (hull (place x cornerrow (wall-sphere-top-front c thick-wall? use-border?))
                    (place (+ x step) cornerrow (wall-sphere-top-front c thick-wall? use-border?))
                    (place 0.7 cornerrow (wall-sphere-bottom-front c thick-wall?)))))
     (if-not use-border?
       ()
       (union (top-cover 0.50 1.70 (* cornerrow index-finger-cover-multiplier) cornerrow)
              (top-cover 1.59 2.36 (* cornerrow middle-finger-cover-multiplier) cornerrow) ;; was 3.32
              (top-cover 2.34 3.31 (* cornerrow ring-finger-cover-multiplier) cornerrow)
              (if use-wide-pinky?
                (top-cover (- ncols 0.5 (if hide-last-pinky? 1 0)) (- ncols -0.05) (* cornerrow pinky-finger-cover-multiplier) cornerrow)
                ())))
     (apply union
            (for [x (range 2 lastrow)]
              (union
               (hull (place (- x 0.5) cornerrow (translate [0 1 1] (wall-sphere-bottom-front c thick-wall?)))
                     (place (+ x 0.5) cornerrow (translate [0 1 1] (wall-sphere-bottom-front c thick-wall?)))
                     (key-place c x cornerrow (cmn/web-post-bl c web-thickness))
                     (key-place c x cornerrow (cmn/web-post-br c web-thickness)))
               (hull (place (- x 0.5) cornerrow (translate [0 1 1] (wall-sphere-bottom-front c thick-wall?)))
                     (key-place c x cornerrow (cmn/web-post-bl c web-thickness))
                     (key-place c (- x 1) cornerrow (cmn/web-post-br c web-thickness))))))
     (hull (place (right-wall-column c) cornerrow (translate [0 1 1] (wall-sphere-bottom-front c thick-wall?)))
           (place (- (right-wall-column c) 1) cornerrow (translate [0 1 1] (wall-sphere-bottom-front c thick-wall?)))
           (key-place c penultcol cornerrow (cmn/web-post-bl c web-thickness))
           (key-place c penultcol cornerrow (cmn/web-post-br c web-thickness)))
     (hull (place (+ antecol 0.5) cornerrow (translate [0 1 1] (wall-sphere-bottom-front c thick-wall?)))
           (place (- (right-wall-column c) 1) cornerrow (translate [0 1 1] (wall-sphere-bottom-front c thick-wall?)))
           (key-place c antecol cornerrow (cmn/web-post-br c web-thickness))
           (key-place c penultcol cornerrow (cmn/web-post-bl c web-thickness)))
     (hull (place 0.7 cornerrow (translate [0 1 1] (wall-sphere-bottom-front c thick-wall?)))
           (place 1.7 cornerrow (translate [0 1 1] (wall-sphere-bottom-front c thick-wall?)))
           (key-place c 1 cornerrow (cmn/web-post-bl c web-thickness))
           (key-place c 1 cornerrow (cmn/web-post-br c web-thickness))))))

(defn back-wall [c]
  (let [ncols                   (get c :configuration-ncols)
        manuform-offset?        (get c :configuration-manuform-offset?)
        use-border?             (get c :configuration-use-border?)
        thick-wall?             (get c :configuration-thick-wall?)
        web-thickness (get c :configuration-web-thickness)
        penultcol               (fpenultcol ncols)
        antecol                 (fantecol ncols)
        rows                    (frows c)
        back-row                (first rows)
        step                    wall-step
        wall-sphere-top-backtep 0.05
        place                   (partial case-place c)
        front-top-cover         (fn [x-start x-end y-start y-end]
                                  (apply union
                                         (for [x (range-inclusive x-start (- x-end wall-sphere-top-backtep) wall-sphere-top-backtep)
                                               y (range-inclusive y-start (- y-end wall-sphere-top-backtep) wall-sphere-top-backtep)]
                                           (hull (place x y (wall-sphere-top-back c thick-wall? use-border?))
                                                 (place (+ x wall-sphere-top-backtep) y (wall-sphere-top-back c thick-wall? use-border?))
                                                 (place x (+ y wall-sphere-top-backtep) (wall-sphere-top-back c thick-wall? use-border?))
                                                 (place (+ x wall-sphere-top-backtep) (+ y wall-sphere-top-backtep) (wall-sphere-top-back c thick-wall? use-border?))))))
        top-cover-length        (if manuform-offset? 0.45 0.3)]
    (union
     (apply union
            (for [x (range-inclusive left-wall-column (- (right-wall-column c) step) step)]
              (hull (place x (back-y c) (wall-sphere-top-back c thick-wall? use-border?))
                    (place (+ x step) (back-y c) (wall-sphere-top-back c thick-wall? use-border?))
                    (place x (back-y c) (wall-sphere-bottom-back c thick-wall?))
                    (place (+ x step) (back-y c) (wall-sphere-bottom-back c thick-wall?)))))
     (apply union
            (for [x (range-inclusive left-wall-column (- (right-wall-column c) step) step)]
              (bottom-hull c (place x (back-y c) (wall-sphere-bottom-back c thick-wall?))
                           (place (+ x step) (back-y c) (wall-sphere-bottom-back c thick-wall?)))))

     (if (and (> ncols 4) use-border?)
       (union (front-top-cover 3.56 4.44 (back-y c) (+ (back-y c) top-cover-length))
              (front-top-cover 4.3 (right-wall-column c) (back-y c) (+ (back-y c) top-cover-length)))
       ())

     (hull (place left-wall-column (back-y c) (translate [1 -1 1] (wall-sphere-bottom-back c thick-wall?)))
           (place (+ left-wall-column 1) (back-y c) (translate [0 -1 1] (wall-sphere-bottom-back c thick-wall?)))
           (key-place c 0 back-row (cmn/web-post-tl c web-thickness))
           (key-place c 0 back-row (cmn/web-post-tr c web-thickness)))

     (hull (place penultcol (back-y c) (translate [0 -1 1] (wall-sphere-bottom-back c thick-wall?)))
           (place (right-wall-column c) (back-y c) (translate [0 -1 1] (wall-sphere-bottom-back c thick-wall?)))
           (key-place c penultcol back-row (cmn/web-post-tl c web-thickness))
           (key-place c penultcol back-row (cmn/web-post-tr c web-thickness)))

     (apply union
            (for [x (range 1 penultcol)]
              (union
               (hull (place (- x 0.5) (back-y c) (translate [0 -1 1] (wall-sphere-bottom-back c thick-wall?)))
                     (place (+ x 0.5) (back-y c) (translate [0 -1 1] (wall-sphere-bottom-back c thick-wall?)))
                     (key-place c x back-row (cmn/web-post-tl c web-thickness))
                     (key-place c x back-row (cmn/web-post-tr c web-thickness)))
               (hull (place (- x 0.5) (back-y c) (translate [0 -1 1] (wall-sphere-bottom-back c thick-wall?)))
                     (key-place c x back-row (cmn/web-post-tl c web-thickness))
                     (key-place c (- x 1) back-row (cmn/web-post-tr c web-thickness))))))
     (hull (place (- 4 0.5) (back-y c) (translate [0 -1 1] (wall-sphere-bottom-back c thick-wall?)))
           (place penultcol (back-y c) (translate [0 -1 1] (wall-sphere-bottom-back c thick-wall?)))
           (key-place c antecol back-row (cmn/web-post-tr c web-thickness))
           (key-place c penultcol back-row (cmn/web-post-tl c web-thickness))))))

(defn right-wall [c]
  (let [ncols        (get c :configuration-ncols)
        use-lastrow? (get c :configuration-use-lastrow?)
        use-numrow?  (get c :configuration-use-numrow?)
        use-border?  (get c :configuration-use-border?)
        thick-wall?  (get c :configuration-thick-wall?)
        web-thickness (get c :configuration-web-thickness)
        penultcol    (fpenultcol ncols)
        rows         (frows c)
        lastrow      (flastrow-lightcycle use-lastrow?)
        cornerrow    (fcornerrow-lightcycle use-lastrow?)
        wall-stop    (if use-lastrow? cornerrow cornerrow)
        place        (partial case-place c)]
    (union
     (apply union
            (map (partial apply hull)
                 (partition 2 1
                            (for [scale (range-inclusive 0 1 0.01)]
                              (let [x (scale-to-range wall-stop (back-y c) scale)]
                                (hull (place (right-wall-column c) x (wall-sphere-top c thick-wall? scale use-border?))
                                      (place (right-wall-column c) x (wall-sphere-bottom c thick-wall? scale))))))))

     (apply union
            (map (partial apply hull)
                 (partition 2 1
                            (for [scale (range-inclusive 0 1 0.01)]
                              (let [x (scale-to-range wall-stop (back-y c) scale)]
                                (bottom-hull c (place (right-wall-column c) x (wall-sphere-top c thick-wall? scale use-border?))
                                             (place (right-wall-column c) x (wall-sphere-bottom c thick-wall? scale))))))))

     (apply union
            (concat
             (for [x (range (if use-numrow? 0 1) lastrow)]
               (union
                (hull (place (right-wall-column c) x (translate [-1 0 1] (wall-sphere-bottom c thick-wall? 0.5)))
                      (key-place c penultcol x (cmn/web-post-br c web-thickness))
                      (key-place c penultcol x (cmn/web-post-tr c web-thickness)))))
             (for [x (range (if use-numrow? 0 1) cornerrow)]
               (union
                (hull (place (right-wall-column c) x (translate [-1 0 1] (wall-sphere-bottom c thick-wall? 0.5)))
                      (place (right-wall-column c) (inc x) (translate [-1 0 1] (wall-sphere-bottom c thick-wall? 0.5)))
                      (key-place c penultcol x (cmn/web-post-br c web-thickness))
                      (key-place c penultcol (inc x) (cmn/web-post-tr c web-thickness)))))
             [(union
               (hull (place (right-wall-column c) (first rows) (translate [-1 0 1] (wall-sphere-bottom c thick-wall? 0.5)))
                     (place (right-wall-column c) (back-y c) (translate [-1 -1 1] (wall-sphere-bottom c thick-wall? 1)))
                     (key-place c penultcol (first rows) (cmn/web-post-tr c web-thickness)))
               (hull (place (right-wall-column c) cornerrow (translate [-1 0 1] (wall-sphere-bottom c thick-wall? 0.5)))
                     (place (right-wall-column c) cornerrow (translate [-1 1 1] (wall-sphere-bottom c thick-wall? 0)))
                     (key-place c penultcol cornerrow (cmn/web-post-br c web-thickness))))])))))

(defn left-wall [c]
  (let [thumb-count      (get c :configuration-thumb-count)
        rows             (frows c)
        use-numrow?      (get c :configuration-use-numrow?)
        use-border?      (get c :configuration-use-border?)
        thick-wall?      (get c :configuration-thick-wall?)
        web-thickness (get c :configuration-web-thickness)
        place            (partial case-place c)
        thumb-where      (case thumb-count :two 0 1)
        finish-left-wall (case thumb-count :two 2.35 1.6666)]
    (union
     (apply union
            (for [x (range-inclusive (dec (first rows)) (- finish-left-wall wall-step) wall-step)]
              (hull (place left-wall-column x (wall-sphere-top-front c thick-wall? use-border?))
                    (place left-wall-column (+ x wall-step) (wall-sphere-top-front c thick-wall? use-border?))
                    (place left-wall-column x (wall-sphere-bottom-front c thick-wall?))
                    (place left-wall-column (+ x wall-step) (wall-sphere-bottom-front c thick-wall?)))))
     (apply union
            (for [x (range-inclusive (dec (first rows)) (- finish-left-wall wall-step) wall-step)]
              (bottom-hull c (place left-wall-column x (wall-sphere-bottom-front c thick-wall?))
                           (place left-wall-column (+ x wall-step) (wall-sphere-bottom-front c thick-wall?)))))
     (hull (place left-wall-column (dec (first rows)) (wall-sphere-top-front c thick-wall? use-border?))
           (place left-wall-column (dec (first rows)) (wall-sphere-bottom-front c thick-wall?))
           (place left-wall-column (back-y c) (wall-sphere-top-back c thick-wall? use-border?))
           (place left-wall-column (back-y c) (wall-sphere-bottom-back c thick-wall?)))

     (bottom-hull c (place left-wall-column (dec (first rows)) (wall-sphere-bottom-front c thick-wall?))
                  (place left-wall-column (back-y c) (wall-sphere-bottom-back c thick-wall?)))
     (if use-numrow?
       (color [0 0 1] (hull (place left-wall-column 0 (translate [1 -1 1] (wall-sphere-bottom-back c thick-wall?)))
                            (place left-wall-column 1 (translate [1 0 1] (wall-sphere-bottom-back c thick-wall?)))
                            (translate [1 0 0] (key-place c 0 0 (cmn/web-post-tl c web-thickness)))
                            (key-place c 0 1 (cmn/web-post-tl c web-thickness))))
       ())
     (color [0 1 0] (hull (place left-wall-column 1 (translate [1 -1 1] (wall-sphere-bottom-back c thick-wall?)))
                          (place left-wall-column 2 (translate [1 0 1] (wall-sphere-bottom-back c thick-wall?)))
                          (place left-wall-column 2 (translate [1 0 1] (wall-sphere-bottom-back c thick-wall?)))
                          (translate [0 0 0] (key-place c 0 1 (cmn/web-post-tl c web-thickness)))
                          (key-place c 0 1 (cmn/web-post-bl c web-thickness))
                          (key-place c 0 2 (cmn/web-post-tl c web-thickness))))
     (color [0.8 1 0] (hull (place left-wall-column 2 (translate [1 -1 1] (wall-sphere-bottom-back c thick-wall?)))
                            (place left-wall-column 2 (translate [1  0 1] (wall-sphere-bottom-back c thick-wall?)))
                            (key-place c 0 2 (cmn/web-post-tl c web-thickness))
                            (key-place c 0 2 (cmn/web-post-bl c web-thickness))
                            (place left-wall-column 3 (translate [2 10 3] (wall-sphere-bottom-back c thick-wall?)))
                            (key-place c 0 3 (cmn/web-post-tl c web-thickness))))
     (case thumb-count
       :two (color [0 1 1] (hull (place left-wall-column 2.5 (translate [1 0 1] (wall-sphere-bottom-back c thick-wall?)))
                                 (key-place c 0 3 (cmn/web-post-tl c web-thickness))
                                 (key-place c 0 3 (cmn/web-post-bl c web-thickness))
                                 (place left-wall-column 3.5 (translate [1 0 1] (wall-sphere-bottom-back c thick-wall?)))))
       (color [0 0 0] (hull (place left-wall-column finish-left-wall  (translate [1 0 1] (wall-sphere-bottom-front c thick-wall?)))
                            (thumb-place c 1 thumb-where (cmn/web-post-tr c web-thickness))
                            (place left-wall-column finish-left-wall  (translate [1 -1 1] (wall-sphere-bottom-front c thick-wall?)))
                            (key-place   c 0 3 (case thumb-count :two (cmn/web-post-bl c web-thickness) (cmn/web-post-tl c web-thickness)))))))))

(defn thumb-back-wall [c]
  (let [thumb-count                      (get c :configuration-thumb-count)
        use-border?                      (get c :configuration-use-border?)
        thick-wall?                      (get c :configuration-thick-wall?)
        web-thickness (get c :configuration-web-thickness)
        step                             wall-step
        local-back-y                     (thumb-back-y c)
        thumb-range                      (case thumb-count :five (/ 5 2) :six (/ 5 2) :eight (/ 5 2) (/ 3 2))
        back-thumb-position              (case thumb-count :two 0 1)
        thumb-back-to-left-wall-position (case thumb-count :two 2.35 1.6666)]
    (union
     (apply union
            (for [x (range-inclusive 0.5 (- (+ thumb-range 0.05) step) step)]
              (hull (thumb-place c x local-back-y (wall-sphere-top-back c thick-wall? use-border?))
                    (thumb-place c (+ x step) local-back-y (wall-sphere-top-back c thick-wall? use-border?))
                    (thumb-place c x local-back-y (wall-sphere-bottom-back c thick-wall?))
                    (thumb-place c (+ x step) local-back-y (wall-sphere-bottom-back c thick-wall?)))))
     (apply union
            (for [x (range-inclusive 0.5 (- (+ thumb-range 0.05) step) step)]
              (bottom-hull c (thumb-place c x local-back-y (wall-sphere-bottom-back c thick-wall?))
                           (thumb-place c (+ x step) local-back-y (wall-sphere-bottom-back c thick-wall?)))))
     (hull (thumb-place c 0.5 local-back-y (wall-sphere-top-back c thick-wall? use-border?))
           (thumb-place c 0.5 local-back-y (wall-sphere-bottom-back c thick-wall?))
           (case-place  c left-wall-column thumb-back-to-left-wall-position (wall-sphere-top-front c thick-wall? use-border?)))
     (hull (thumb-place c 0.5 local-back-y (wall-sphere-bottom-back c thick-wall?))
           (case-place  c left-wall-column thumb-back-to-left-wall-position (wall-sphere-top-front c thick-wall? use-border?))
           (case-place  c left-wall-column thumb-back-to-left-wall-position (wall-sphere-bottom-front c thick-wall?)))
     (bottom-hull c (thumb-place c 0.5 local-back-y (wall-sphere-bottom-back c thick-wall?))
                  (case-place  c left-wall-column thumb-back-to-left-wall-position (wall-sphere-bottom-front c thick-wall?)))
     (hull
      (thumb-place c 0.5 (thumb-back-y c) (wall-sphere-bottom-back c thick-wall?))
      (thumb-place c 1 back-thumb-position (cmn/web-post-tr c web-thickness))
      (thumb-place c (/ 3 2) (thumb-back-y c) (wall-sphere-bottom-back c thick-wall?))
      (thumb-place c 1 back-thumb-position (cmn/web-post-tl c web-thickness)))
     (hull
      (thumb-place c (+ (/ 3 2) 0.05) (thumb-back-y c) (wall-sphere-bottom-back c thick-wall?))
      (thumb-place c (/ 3 2) (thumb-back-y c) (wall-sphere-bottom-back c thick-wall?))
      (thumb-place c 1 back-thumb-position (cmn/web-post-tl c web-thickness))
      (thumb-place c 1 back-thumb-position (cmn/web-post-tl c web-thickness))))))

(defn thumb-left-wall [c]
  (let [thumb-count      (get c :configuration-thumb-count)
        use-border?      (get c :configuration-use-border?)
        thick-wall?      (get c :configuration-thick-wall?)
        thumb-alpha      (get c :configuration-thumb-alpha)
        web-thickness (get c :configuration-web-thickness)
        step             wall-step
        place            (partial thumb-place c)
        column           (case thumb-count :five 2 :six 2 :eight 2 1)
        left-wall-length (case thumb-count :two (- 1.18 (/ thumb-alpha 1.5)) (- 2.24 thumb-alpha))]
    (union
     (apply union
            (for [x (range-inclusive (+ -1 0.07) (- left-wall-length step) step)]
              (hull (place (thumb-left-wall-column c) x (wall-sphere-top-front c thick-wall? use-border?))
                    (place (thumb-left-wall-column c) (+ x step) (wall-sphere-top-front c thick-wall? use-border?))
                    (place (thumb-left-wall-column c) x (wall-sphere-bottom-front c thick-wall?))
                    (place (thumb-left-wall-column c) (+ x step) (wall-sphere-bottom-front c thick-wall?)))))
     (apply union
            (for [x (range-inclusive (+ -1 0.07) (- left-wall-length step) step)]
              (bottom-hull c (place (thumb-left-wall-column c) x (wall-sphere-bottom-front c thick-wall?))
                           (place (thumb-left-wall-column c) (+ x step) (wall-sphere-bottom-front c thick-wall?)))))
     (case thumb-count
       :two ()
       (union (hull (place (thumb-left-wall-column c) 1.95 (wall-sphere-top-front c thick-wall? use-border?))
                    (place (thumb-left-wall-column c) 1.95 (wall-sphere-bottom-front c thick-wall?))
                    (place (thumb-left-wall-column c) (thumb-back-y c) (wall-sphere-top-back c thick-wall? use-border?))
                    (place (thumb-left-wall-column c) (thumb-back-y c) (wall-sphere-bottom-back c thick-wall?)))
              (hull (place (thumb-left-wall-column c) (thumb-back-y c) (translate [1 -1 1] (wall-sphere-bottom-back c thick-wall?)))
                    (place (thumb-left-wall-column c) 0 (translate [1 0 1] (wall-sphere-bottom-back c thick-wall?)))
                    (place column 1 (cmn/web-post-tl c web-thickness))
                    (place column 1 (cmn/web-post-bl c web-thickness)))
              (hull (place (thumb-left-wall-column c) 0 (translate [1 0 1] (wall-sphere-bottom-back c thick-wall?)))
                    (place column 0 (cmn/web-post-tl c web-thickness))
                    (place column 1 (cmn/web-post-bl c web-thickness)))))
     (hull
      (place (thumb-left-wall-column c) -0.1 (translate [1 0 1] (wall-sphere-bottom-back c thick-wall?)))
      (place (thumb-left-wall-column c) -1   (translate [1 0 1] (wall-sphere-bottom-back c thick-wall?)))
      (place column 0 (cmn/web-post-tl c web-thickness))
      (place column 0 (cmn/web-post-bl c web-thickness)))
     (hull
      (place (thumb-left-wall-column c) -1 (translate [1 0 1] (wall-sphere-bottom-back c thick-wall?)))
      (place column -1 (cmn/web-post-tl c web-thickness))
      (place column 0 (cmn/web-post-bl c web-thickness)))
     (hull
      (place (thumb-left-wall-column c) -1 (translate [1 0 1] (wall-sphere-bottom-back c thick-wall?)))
      (place (thumb-left-wall-column c) (+ -1 0.07) (translate [1 1 1] (wall-sphere-bottom-front c thick-wall?)))
      (place column -1 (cmn/web-post-tl c web-thickness))
      (place column -1 (cmn/web-post-bl c web-thickness))))))

(defn thumb-front-wall [c]
  (let [thumb-count  (get c :configuration-thumb-count)
        use-lastrow? (get c :configuration-use-lastrow?)
        use-border?  (get c :configuration-use-border?)
        thick-wall?  (get c :configuration-thick-wall?)
        web-thickness (get c :configuration-web-thickness)
        cornerrow    (fcornerrow-lightcycle use-lastrow?)
        step         wall-step ;;0.1
        place        (partial thumb-place c)
        plate-height (/ (- sa-double-length (mount-height c)) 2)
        thumb-bl     (->> (cmn/web-post-bl c web-thickness)
                          (translate [0  (- plate-height) 0]))
        thumb-br     (->> (cmn/web-post-br c web-thickness)
                          (translate [-0 (- plate-height) 0]))
        thumb-range  (case thumb-count :five (/ 5 2) :six (/ 5 2) :eight (/ 5 2) (/ 3 2))]
    (union
     (apply union
            (for [x (range-inclusive thumb-right-wall (- (+ thumb-range 0.05) step) step)]
              (hull (place x thumb-front-row (wall-sphere-top-front c thick-wall? use-border?))
                    (place (+ x step) thumb-front-row (wall-sphere-top-front c thick-wall? use-border?))
                    (place x thumb-front-row (wall-sphere-bottom-front c thick-wall?))
                    (place (+ x step) thumb-front-row (wall-sphere-bottom-front c thick-wall?)))))
     (apply union
            (for [x (range-inclusive thumb-right-wall (- (+ thumb-range 0.05) step) step)]
              (bottom-hull c (place x thumb-front-row (wall-sphere-bottom-front c thick-wall?))
                           (place (+ x step) thumb-front-row (wall-sphere-bottom-front c thick-wall?)))))

     (hull (place thumb-right-wall thumb-front-row (wall-sphere-top-front c thick-wall? use-border?))
           (place thumb-right-wall thumb-front-row (wall-sphere-bottom-front c thick-wall?))
           (case-place c 0.5 cornerrow (wall-sphere-top-front c thick-wall? use-border?)))
     (hull (place thumb-right-wall thumb-front-row (wall-sphere-bottom-front c thick-wall?))
           (case-place c 0.5 cornerrow (wall-sphere-top-front c thick-wall? use-border?)))
     (bottom-hull c (place thumb-right-wall thumb-front-row (wall-sphere-bottom-front c thick-wall?))
                  (case-place c 0.7 cornerrow (wall-sphere-bottom-front c thick-wall?)))
     (hull (place thumb-right-wall thumb-front-row (wall-sphere-bottom-front c thick-wall?))
           (case-place c 0.5 cornerrow (wall-sphere-top-front c thick-wall? use-border?))
           (case-place c 0.7 cornerrow (wall-sphere-bottom-front c thick-wall?)))

     (hull (place thumb-right-wall thumb-front-row (wall-sphere-bottom-front c thick-wall?))
           (key-place c 1 cornerrow (cmn/web-post-bl c web-thickness))
           (place 0 -0.5 thumb-br)
           (place 0 -0.5 (cmn/web-post-br c web-thickness))
           (case-place c 0.7 cornerrow (wall-sphere-bottom-front c thick-wall?)))

     (hull (place thumb-right-wall thumb-front-row (translate [0 1 1] (wall-sphere-bottom-front c thick-wall?)))
           (place (+ 0.5 0.05) thumb-front-row (translate [0 1 1] (wall-sphere-bottom-front c thick-wall?)))
           (place 0 -1   (cmn/web-post-bl c web-thickness))
           (place 0 -1   (cmn/web-post-br c web-thickness)))
     (hull (place (+ 0.5 0.05) thumb-front-row (translate [0 1 1] (wall-sphere-bottom-front c thick-wall?)))
           (place (+ (/ 3 2) 0.05) thumb-front-row (translate [0 1 1] (wall-sphere-bottom-front c thick-wall?)))
           (place 0 -1   (cmn/web-post-bl c web-thickness))
           (place 1 -1   (cmn/web-post-bl c web-thickness))
           (place 1 -1   (cmn/web-post-br c web-thickness)))
     (case thumb-count
       :two ()
       :three ()
       (hull (place (+ (/ 3 2) 0.05) thumb-front-row (translate [0 1 1] (wall-sphere-bottom-front c thick-wall?)))
             (place (+ (/ 5 2) 0.05) thumb-front-row (translate [1 1 1] (wall-sphere-bottom-front c thick-wall?)))
             (place 1            -1              (cmn/web-post-bl c web-thickness))
             (place 2            -0.5            thumb-bl)
             (place 2            -0.5            thumb-br)
             (place 2            -1              (cmn/web-post-bl c web-thickness)))))))

(defn frj9-start [c]
  (let [use-numrow? (get c :configuration-use-numrow?)]
    [-25 (if use-numrow? 55 35) 0]))

(defn fusb-holder-position [c]
  (let [use-numrow? (get c :configuration-use-numrow?)]
    [-10 (if use-numrow? 55 35) 0]))

; Offsets for the controller/trrs external holder cutout
(defn external-holder-offset [c]
  (let [use-external-holder? (get c :configuration-use-external-holder?)]
    (if use-external-holder? 0 -3.5)))

; Cutout for controller/trrs jack holder
(defn external-holder-ref [c]
  (let [tenting-angle (get c :configuration-tenting-angle)]
    (case tenting-angle
      0.4487989505128276  [-27 45]    ;; pi/7
      0.39269908169872414 [-30 45]    ;; pi/8
      0.3490658503988659  [-30 45]    ;; pi/9
      0.3141592653589793  [-33 45]    ;; pi/10
      0.28559933214452665 [-36 45]    ;; pi/11
      0.2617993877991494  [-36 45]))) ;; pi/12

(def external-holder-cube   (cube 29.166 80 12.6))
(defn external-holder-position [c]
  (map + [(+ 18.8 (external-holder-offset c)) 18.7 1.3] [(first (external-holder-ref c)) (second (external-holder-ref c)) 2]))
(defn external-holder-space [c]
  (translate (map + (external-holder-position c) [-1.5 -2 3]) external-holder-cube))

#_(defn screw-insert
    "Places screw insert to its place.
   TODO: write me."
    [c column row bottom-radius top-radius height]
    (let [position (key-position c column row (map + (wall-locate2 0 0) [0 (/ (mount-height c) 2) 0]))]
      (->> (screw-insert-shape bottom-radius top-radius height)
           (translate [(first position) (second position) (/ height 2)]))))

(defn screw-placement [c bottom-radius top-radius height]
  (let [lastrow           (if (get c :configuration-use-lastrow?) 3.99 3.55)
        toprow            (if (get c :configuration-use-numrow?) -0.12 0.8)
        ncols             (get c :configuration-ncols)
        ncold-coefficient (case ncols
                            4 0.77
                            5 0.8
                            6 0.82
                            7 0.9
                            8 0.91
                            1)
        lastcol           (* ncols ncold-coefficient)
        middlecol         (case ncols
                            4 2
                            5 1.7
                            6 2
                            2)
        middlerow         (case ncols
                            4 1.5
                            5 3
                            6 3
                            3)]
    (union (screw-insert c -1.5      4.9       bottom-radius top-radius height)
           (screw-insert c 2         toprow    bottom-radius top-radius height)
           (screw-insert c -0.75     2         bottom-radius top-radius height)
           (screw-insert c middlerow lastrow   bottom-radius top-radius height)
           (screw-insert c lastcol   lastrow   bottom-radius top-radius height))))

(defn new-case [c]
  (union (front-wall c)
         (right-wall c)
         (back-wall c)
         (left-wall c)
         (color [1 0 1] (thumb-back-wall c))
         (color [0 1 0] (thumb-left-wall c))
         (color [0 0 1] (thumb-front-wall c))))

;;;;;;;;;;;;;;;;
;;Final Export ;;
;;;;;;;;;;;;;;;;;;

(defn dactyl-top-right [c]
  (let [use-external-holder? (get c :configuration-use-external-holder?)
        use-screw-inserts? (get c :configuration-use-screw-inserts?)]
    (difference
     (union (key-holes c)
            (connectors c)
            (thumb c)
            (difference (union  (if (get c :configuration-use-case?) (new-case c))
                               (if use-screw-inserts? (screw-insert-outers screw-placement c) ())
                               (if-not use-external-holder? (cmn/usb-holder fusb-holder-position c) ()))
                        (if-not use-external-holder?
                          (union (cmn/rj9-space frj9-start c) (cmn/usb-holder-hole fusb-holder-position c))
                          (external-holder-space c))
                        (if use-screw-inserts? (screw-insert-holes screw-placement c) ()))
            (if (get c :configuration-show-caps?) (caps c) ())
            (if (get c :configuration-show-caps?) (thumbcaps c) ())
            (if-not use-external-holder? (cmn/rj9-holder frj9-start c) ()))
     (translate [0 0 -60] (cube 350 350 120)))))

(defn dactyl-top-left [c]
  (mirror [-1 0 0] (dactyl-top-right c)))

(defn dactyl-plate-right [c]
  (let [use-screw-inserts? (get c :configuration-use-screw-inserts?)]
    (cut
     (translate [0 0 -0.1]
                (difference (union (new-case c)
                                   (if use-screw-inserts? (screw-insert-outers screw-placement c) ()))
                            (if use-screw-inserts? (translate [0 0 -10] (screw-insert-screw-holes screw-placement c)) ()))))))

(defn dactyl-plate-left [c]
  (mirror [-1 0 0] (dactyl-plate-right c)))

(def c {:configuration-ncols                5
        :configuration-use-numrow?          false
        :configuration-use-lastrow?         false
        :configuration-thumb-count          :two
        :configuration-switch-type          :box
        :configuration-use-wide-pinky?      false
        :configuration-hide-last-pinky?     false

        :configuration-alpha                (/ pi 12)
        :configuration-beta                 (/ pi 24)
        :configuration-tenting-angle        (/ pi 7)
        :configuration-thumb-alpha          (/ pi 12)
        :configuration-thumb-beta           (/ pi 36)
        :configuration-thumb-tenting-angle  (/ pi 12)

        :configuration-use-external-holder? false

        :configuration-use-hotswap?         false
        :configuration-thumb-offset-x       -54
        :configuration-thumb-offset-y       -45
        :configuration-thumb-offset-z       30
        :configuration-z-offset             15
        :configuration-manuform-offset?     true
        :configuration-use-border?          true
        :configuration-thick-wall?          true

        :configuration-use-screw-inserts?   false
        :configuration-show-caps?           false})

#_(spit "things/lightcycle-cherry-top-right.scad"
        (write-scad (dactyl-top-right c)))

#_(spit "things/light-cycle-plate-right.scad"
        (write-scad (dactyl-plate-right c)))
