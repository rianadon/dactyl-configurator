(ns dactyl-generator.handler
  (:require [dactyl-generator.generator :as g]))

(def pi Math/PI)
;; (defn generate-manuform [req]
;;   (let [p                                 (:form-params req)
;;         param-ncols                       (parse-int (get p "keys.columns"))
;;         param-nrows                       (parse-int (get p "keys.rows"))
;;         param-thumb-count                 (case (get p "keys.thumb-count")
;;                                             "two" :two
;;                                             "three" :three
;;                                             "three-mini" :three-mini
;;                                             "four" :four
;;                                             "five" :five
;;                                             :six)
;;         param-last-row-count              (case (get p "keys.last-row")
;;                                             "zero" :zero
;;                                             "full" :full
;;                                             :two)
;;         param-switch-type                 (case (get p "keys.switch-type")
;;                                             "mx" :mx
;;                                             "mx-snap-in" :mx-snap-in
;;                                             "alps" :alps
;;                                             "choc" :choc
;;                                             "kailh" :kailh
;;                                             :box)
;;         param-inner-column                (case (get p "keys.inner-column")
;;                                             "innie" :innie
;;                                             "outie" :outie
;;                                             :normie)
;;         param-hide-last-pinky             (parse-bool (get p "keys.hide-last-pinky"))

;;         param-alpha                       (parse-int (get p "curve.alpha"))
;;         param-pinky-alpha                 (parse-int (get p "curve.pinky-alpha"))
;;         param-beta                        (parse-int (get p "curve.beta"))
;;         param-centercol                   (parse-int (get p "curve.centercol"))
;;         param-tenting-angle               (parse-int (get p "curve.tenting"))
;;         param-rotate-x-angle              (parse-int (get p "curve.rotate-x"))

;;         param-use-external-holder         (parse-bool (get p "connector.external"))
;;         param-connector-type              (case (get p "connector.type")
;;                                             "none" :none
;;                                             "trrs" :trrs
;;                                             "rj9" :rj9
;;                                             "usb" :usb)
;;         param-use-promicro-usb-hole       (parse-bool (get p "connector.micro-usb"))

;;         param-hotswap                     (parse-bool (get p "form.hotswap"))
;;         param-stagger                     (parse-bool (get p "form.stagger"))
;;         param-keyboard-z-offset           (parse-int (get p "form.height-offset"))
;;         param-web-thickness               (parse-float (get p "form.web-thickness"))
;;         param-wall-thickness              (parse-float (get p "form.wall-thickness"))
;;         param-wide-pinky                  (parse-bool (get p "form.wide-pinky"))
;;         param-wire-post                   (parse-bool (get p "form.wire-post"))
;;         param-screw-inserts               (parse-bool (get p "form.screw-inserts"))
;;         param-thumb-cluster-offset-x      (parse-float (get p "form.thumb-cluster-offset-x"))
;;         param-thumb-cluster-offset-y      (parse-float (get p "form.thumb-cluster-offset-y"))
;;         param-thumb-cluster-offset-z      (parse-float (get p "form.thumb-cluster-offset-z"))
;;         param-custom-thumb-cluster        (parse-bool (get p "form.custom-thumb-cluster"))
;;         param-thumb-top-right-offset-x    (parse-float (get p "form.thumb-top-right-offset-x"))
;;         param-thumb-top-right-offset-y    (parse-float (get p "form.thumb-top-right-offset-y"))
;;         param-thumb-top-right-offset-z    (parse-float (get p "form.thumb-top-right-offset-z"))
;;         param-thumb-top-right-tenting-x   (parse-float (get p "form.thumb-top-right-tenting-x"))
;;         param-thumb-top-right-tenting-y   (parse-float (get p "form.thumb-top-right-tenting-y"))
;;         param-thumb-top-right-tenting-z   (parse-float (get p "form.thumb-top-right-tenting-z"))
;;         param-thumb-top-left-offset-x     (parse-float (get p "form.thumb-top-left-offset-x"))
;;         param-thumb-top-left-offset-y     (parse-float (get p "form.thumb-top-left-offset-y"))
;;         param-thumb-top-left-offset-z     (parse-float (get p "form.thumb-top-left-offset-z"))
;;         param-thumb-top-left-tenting-x    (parse-float (get p "form.thumb-top-left-tenting-x"))
;;         param-thumb-top-left-tenting-y    (parse-float (get p "form.thumb-top-left-tenting-y"))
;;         param-thumb-top-left-tenting-z    (parse-float (get p "form.thumb-top-left-tenting-z"))
;;         param-thumb-middle-left-offset-x  (parse-float (get p "form.thumb-middle-left-offset-x"))
;;         param-thumb-middle-left-offset-y  (parse-float (get p "form.thumb-middle-left-offset-y"))
;;         param-thumb-middle-left-offset-z  (parse-float (get p "form.thumb-middle-left-offset-z"))
;;         param-thumb-middle-left-tenting-x (parse-float (get p "form.thumb-middle-left-tenting-x"))
;;         param-thumb-middle-left-tenting-y (parse-float (get p "form.thumb-middle-left-tenting-y"))
;;         param-thumb-middle-left-tenting-z (parse-float (get p "form.thumb-middle-left-tenting-z"))
;;         param-index-y                     (parse-float (get p "form.stagger-index-y"))
;;         param-index-z                     (parse-float (get p "form.stagger-index-z"))
;;         param-middle-y                    (parse-float (get p "form.stagger-middle-y"))
;;         param-middle-z                    (parse-float (get p "form.stagger-middle-z"))
;;         param-ring-y                      (parse-float (get p "form.stagger-ring-y"))
;;         param-ring-z                      (parse-float (get p "form.stagger-ring-z"))
;;         param-pinky-y                     (parse-float (get p "form.stagger-pinky-y"))
;;         param-pinky-z                     (parse-float (get p "form.stagger-pinky-z"))

;;         stagger-index                     [0 param-index-y param-index-z]
;;         stagger-middle                    [0 param-middle-y param-middle-z]
;;         stagger-ring                      [0 param-ring-y param-ring-z]
;;         stagger-pinky                     [0 param-pinky-y param-pinky-z]

;;         param-show-keycaps                (parse-bool (get p "misc.keycaps"))
;;         is-right?                         (parse-bool (get p "misc.right-side"))

;;         param-generate-plate              (get p "generate-plate")
;;         param-generate-json               (get p "generate-json")

;;         generate-plate?                   (some? param-generate-plate)
;;         generate-json?                    (some? param-generate-json)

;;         c                                 {:configuration-nrows                       param-nrows
;;                                            :configuration-ncols                       param-ncols
;;                                            :configuration-thumb-count                 param-thumb-count
;;                                            :configuration-last-row-count              param-last-row-count
;;                                            :configuration-switch-type                 param-switch-type
;;                                            :configuration-inner-column                param-inner-column
;;                                            :configuration-hide-last-pinky?            param-hide-last-pinky

;;                                            :configuration-alpha                       (if generate-json? param-alpha (/ pi param-alpha))
;;                                            :configuration-pinky-alpha                 (if generate-json? param-pinky-alpha (/ pi param-pinky-alpha))
;;                                            :configuration-beta                        (if generate-json? param-beta (/ pi param-beta))
;;                                            :configuration-centercol                   param-centercol
;;                                            :configuration-tenting-angle               (if generate-json? param-tenting-angle (/ pi param-tenting-angle))
;;                                            :configuration-rotate-x-angle              (if generate-json? param-rotate-x-angle (/ pi param-rotate-x-angle))
;;                                            :configuration-plate-projection?           generate-plate?

;;                                            :configuration-use-external-holder?        param-use-external-holder
;;                                            :configuration-connector-type              param-connector-type
;;                                            :configuration-use-promicro-usb-hole?      param-use-promicro-usb-hole

;;                                            :configuration-use-hotswap?                param-hotswap
;;                                            :configuration-thumb-cluster-offset-x      param-thumb-cluster-offset-x
;;                                            :configuration-thumb-cluster-offset-y      param-thumb-cluster-offset-y
;;                                            :configuration-thumb-cluster-offset-z      param-thumb-cluster-offset-z
;;                                            :configuration-custom-thumb-cluster?       param-custom-thumb-cluster
;;                                            :configuration-thumb-top-right-offset-x    param-thumb-top-right-offset-x
;;                                            :configuration-thumb-top-right-offset-y    param-thumb-top-right-offset-y
;;                                            :configuration-thumb-top-right-offset-z    param-thumb-top-right-offset-z
;;                                            :configuration-thumb-top-right-tenting-x   (if generate-json? param-thumb-top-right-tenting-x param-thumb-top-right-tenting-x)
;;                                            :configuration-thumb-top-right-tenting-y   (if generate-json? param-thumb-top-right-tenting-y param-thumb-top-right-tenting-y)
;;                                            :configuration-thumb-top-right-tenting-z   (if generate-json? param-thumb-top-right-tenting-z param-thumb-top-right-tenting-z)
;;                                            :configuration-thumb-top-left-offset-x     param-thumb-top-left-offset-x
;;                                            :configuration-thumb-top-left-offset-y     param-thumb-top-left-offset-y
;;                                            :configuration-thumb-top-left-offset-z     param-thumb-top-left-offset-z
;;                                            :configuration-thumb-top-left-tenting-x    (if generate-json? param-thumb-top-left-tenting-x param-thumb-top-left-tenting-x)
;;                                            :configuration-thumb-top-left-tenting-y    (if generate-json? param-thumb-top-left-tenting-y param-thumb-top-left-tenting-y)
;;                                            :configuration-thumb-top-left-tenting-z    (if generate-json? param-thumb-top-left-tenting-z param-thumb-top-left-tenting-z)
;;                                            :configuration-thumb-middle-left-offset-x  param-thumb-middle-left-offset-x
;;                                            :configuration-thumb-middle-left-offset-y  param-thumb-middle-left-offset-y
;;                                            :configuration-thumb-middle-left-offset-z  param-thumb-middle-left-offset-z
;;                                            :configuration-thumb-middle-left-tenting-x (if generate-json? param-thumb-middle-left-tenting-x param-thumb-middle-left-tenting-x)
;;                                            :configuration-thumb-middle-left-tenting-y (if generate-json? param-thumb-middle-left-tenting-y param-thumb-middle-left-tenting-y)
;;                                            :configuration-thumb-middle-left-tenting-z (if generate-json? param-thumb-middle-left-tenting-z param-thumb-middle-left-tenting-z)
;;                                            :configuration-stagger?                    param-stagger
;;                                            :configuration-stagger-index               stagger-index
;;                                            :configuration-stagger-middle              stagger-middle
;;                                            :configuration-stagger-ring                stagger-ring
;;                                            :configuration-stagger-pinky               stagger-pinky
;;                                            :configuration-z-offset                    param-keyboard-z-offset
;;                                            :configuration-web-thickness               param-web-thickness
;;                                            :configuration-wall-thickness              param-wall-thickness
;;                                            :configuration-show-caps?                  param-show-keycaps
;;                                            :configuration-use-wide-pinky?             param-wide-pinky
;;                                            :configuration-use-wire-post?              param-wire-post
;;                                            :configuration-use-screw-inserts?          param-screw-inserts

;;                                            :is-right?                                 is-right?}
;;         generated-file                    (cond
;;                                             generate-plate? {:file      (g/generate-plate-dm c is-right?)
;;                                                              :part      "plate"
;;                                                              :extension "scad"}
;;                                             generate-json? {:file      (g/generate-json-dm c is-right?)
;;                                                             :part      "any"
;;                                                             :extension "json"}
;;                                             :else {:file      (g/generate-case-dm c is-right?)
;;                                                    :part      (str "case-" (if is-right? "right" "left"))
;;                                                    :extension "scad"})
;;         scad-file                         (get generated-file :file)
;;         part-name                         (get generated-file :part)
;;         date-time                         (current-time)
;;         extension                         (get generated-file :extension)]
;;     {:status  200
;;      :headers {"Content-Type"        "application/octet-stream"
;;                "Content-Disposition" (str "inline; filename=\"manuform-" part-name "-" date-time "." extension "\"")}
;;      :body    scad-file}))

;; (defn generate-lightcycle [req]
;;   (let [p                         (:form-params req)
;;         param-ncols               (parse-int (get p "keys.columns"))
;;         param-use-numrow?         (parse-bool (get p "keys.num-row"))
;;         param-use-lastrow?        (parse-bool (get p "keys.last-row"))
;;         param-thumb-count         (case (get p "keys.thumb-count")
;;                                     "two" :two
;;                                     "three" :three
;;                                     "six" :six
;;                                     "eight" :eight
;;                                     :five)
;;         param-switch-type         (case (get p "keys.switch-type")
;;                                     "mx" :mx
;;                                     "mx-snap-in" :mx-snap-in
;;                                     "alps" :alps
;;                                     "choc" :choc
;;                                     "kailh" :kailh
;;                                     :box)
;;         param-hide-last-pinky     (parse-bool (get p "keys.hide-last-pinky"))
;;         param-alpha               (parse-int (get p "curve.alpha"))
;;         param-beta                (parse-int (get p "curve.beta"))
;;         param-tenting-angle       (parse-int (get p "curve.tenting"))
;;         param-thumb-alpha         (parse-int (get p "curve.thumb-alpha"))
;;         param-thumb-beta          (parse-int (get p "curve.thumb-beta"))
;;         param-thumb-tenting-angle (parse-int (get p "curve.thumb-tenting"))
;;         param-rotate-x-angle      (parse-int (get p "curve.rotate-x"))

;;         param-hotswap             (parse-bool (get p "form.hotswap"))
;;         param-thumb-offset-x      (parse-int (get p "form.thumb-offset-x"))
;;         param-thumb-offset-y      (parse-int (get p "form.thumb-offset-y"))
;;         param-thumb-offset-z      (parse-int (get p "form.thumb-offset-z"))
;;         param-use-wide-pinky      (parse-bool (get p "form.wide-pinky"))
;;         param-z-offset            (parse-int (get p "form.z-offset"))
;;         param-web-thickness       (parse-float (get p "form.web-thickness"))
;;         param-manuform-offset     (parse-bool (get p "form.manuform-offset"))
;;         param-use-border          (parse-bool (get p "form.border"))
;;         param-thick-wall          (parse-bool (get p "form.thick-wall"))

;;         param-use-external-holder (parse-bool (get p "misc.external-holder"))
;;         param-screw-inserts       (parse-bool (get p "misc.screw-inserts"))
;;         param-show-keycaps        (parse-bool (get p "misc.show-keycaps"))
;;         is-right?                 (parse-bool (get p "misc.right-side"))

;;         param-generate-plate      (get p "generate-plate")
;;         param-generate-json       (get p "generate-json")

;;         generate-plate?           (some? param-generate-plate)
;;         generate-json?            (some? param-generate-json)

;;         c                         {:configuration-ncols                param-ncols
;;                                    :configuration-use-numrow?          param-use-numrow?
;;                                    :configuration-use-lastrow?         param-use-lastrow?
;;                                    :configuration-thumb-count          param-thumb-count
;;                                    :configuration-hide-last-pinky?     param-hide-last-pinky
;;                                    :configuration-use-wide-pinky?      param-use-wide-pinky
;;                                    :configuration-switch-type          param-switch-type

;;                                    :configuration-alpha                (if generate-json? param-alpha (/ pi param-alpha))
;;                                    :configuration-beta                 (if generate-json? param-beta (/ pi param-beta))
;;                                    :configuration-tenting-angle        (if generate-json? param-tenting-angle (/ pi param-tenting-angle))
;;                                    :configuration-rotate-x-angle       (if generate-json? param-rotate-x-angle (/ pi param-rotate-x-angle))
;;                                    :configuration-thumb-alpha          (if generate-json? param-thumb-alpha (/ pi param-thumb-alpha))
;;                                    :configuration-thumb-beta           (if generate-json? param-thumb-beta (/ pi param-thumb-beta))
;;                                    :configuration-thumb-tenting-angle  (if generate-json? param-thumb-tenting-angle (/ pi param-thumb-tenting-angle))

;;                                    :configuration-use-external-holder? param-use-external-holder
;;                                    :configuration-use-hotswap?         param-hotswap
;;                                    :configuration-thumb-offset-x       (if generate-json? param-thumb-offset-x (- 0 param-thumb-offset-x))
;;                                    :configuration-thumb-offset-y       (if generate-json? param-thumb-offset-y (- 0 param-thumb-offset-y))
;;                                    :configuration-thumb-offset-z       param-thumb-offset-z
;;                                    :configuration-z-offset             param-z-offset
;;                                    :configuration-web-thickness        param-web-thickness
;;                                    :configuration-manuform-offset?     param-manuform-offset
;;                                    :configuration-use-border?          param-use-border
;;                                    :configuration-thick-wall?          param-thick-wall

;;                                    :configuration-show-caps?           param-show-keycaps

;;                                    :configuration-use-screw-inserts?   param-screw-inserts

;;                                    :is-right?                          is-right?}
;;         generated-file            (cond
;;                                     generate-plate? {:file      (g/generate-plate-dl c is-right?)
;;                                                      :extension "scad"}
;;                                     generate-json? {:file      (g/generate-json-dl c is-right?)
;;                                                     :extension "json"}
;;                                     :else {:file      (g/generate-case-dl c is-right?)
;;                                            :extension "scad"})
;;         scad-file                 (get generated-file :file)
;;         part-name                 (get generated-file :part)
;;         date-time                 (current-time)
;;         extension                 (get generated-file :extension)]
;;     {:status  200
;;      :headers {"Content-Type"        "application/octet-stream"
;;                "Content-Disposition" (str "inline; filename=\"lightcycle-" part-name "-" date-time "." extension "\"")}
;;      :body    scad-file}))

(defn api-generate-manuform [body]
  (let [keys           (get body "keys")
        curve          (get body "curve")
        connector      (get body "connector")
        form           (get body "form")
        index-y        (get form "stagger-index-y" 0)
        index-z        (get form "stagger-index-z" 0)
        middle-y       (get form "stagger-middle-y" 2.8)
        middle-z       (get form "stagger-middle-z" -6.5)
        ring-y         (get form "stagger-ring-y" 0)
        ring-z         (get form "stagger-ring-z" 0)
        pinky-y        (get form "stagger-pinky-y" -13)
        pinky-z        (get form "stagger-pinky-z" 6)
        stagger-index  [0 index-y index-z]
        stagger-middle [0 middle-y middle-z]
        stagger-ring   [0 ring-y ring-z]
        stagger-pinky  [0 pinky-y pinky-z]
        misc           (get body :misc)
        c              {:configuration-ncols                       (get keys "columns" 5)
                        :configuration-nrows                       (get keys "rows" 4)
                        :configuration-thumb-count                 (keyword (get keys "thumb-count" "six"))
                        :configuration-last-row-count              (keyword (get keys "last-row" "two"))
                        :configuration-switch-type                 (keyword (get keys "switch-type" "box"))
                        :configuration-inner-column                (keyword (get keys "inner-column" "normie"))
                        :configuration-hide-last-pinky?            (get keys "hide-last-pinky" false)

                        :configuration-alpha                       (/ pi (get curve "alpha" 12))
                        :configuration-pinky-alpha                 (/ pi (get curve "pinky-alpha" 12))
                        :configuration-beta                        (/ pi (get curve "beta" 36))
                        :configuration-centercol                   (get curve "centercol" 4)
                        :configuration-tenting-angle               (/ pi (get curve "tenting" 15))
                        :configuration-rotate-x-angle              (/ pi (get curve "rotate-x" 15))

                        :configuration-use-external-holder?        (get connector "external" false)
                        :configuration-connector-type              (keyword (get connector "type" "none"))
                        :configuration-use-promicro-usb-hole?      (get connector "micro-usb" false)

                        :configuration-thumb-cluster-offset-x      (get form "thumb-cluster-offset-x" 6)
                        :configuration-thumb-cluster-offset-y      (get form "thumb-cluster-offset-y" -3)
                        :configuration-thumb-cluster-offset-z      (get form "thumb-cluster-offset-z" 7)
                        :configuration-custom-thumb-cluster?       (get form "custom-thumb-cluster" false)
                        :configuration-thumb-top-right-tenting-x   (/ pi (get form "thumb-tenting-x" 10))
                        :configuration-thumb-top-right-tenting-y   (/ pi (get form "thumb-tenting-y" -4))
                        :configuration-thumb-top-right-tenting-z   (/ pi (get form "thumb-tenting-z" 10))
                        :configuration-thumb-top-right-offset-x    (get form "thumb-top-right-offset-x" -15)
                        :configuration-thumb-top-right-offset-y    (get form "thumb-top-right-offset-y" -10)
                        :configuration-thumb-top-right-offset-z    (get form "thumb-top-right-offset-z" 5)
                        :configuration-thumb-top-left-tenting-x    (/ pi (get form "thumb-top-left-tenting-x" 10))
                        :configuration-thumb-top-left-tenting-y    (/ pi (get form "thumb-top-left-tenting-y" -4))
                        :configuration-thumb-top-left-tenting-z    (/ pi (get form "thumb-top-left-tenting-z" 10))
                        :configuration-thumb-top-left-offset-x     (get form "thumb-top-left-offset-x" -35)
                        :configuration-thumb-top-left-offset-y     (get form "thumb-top-left-offset-y" -16)
                        :configuration-thumb-top-left-offset-z     (get form "thumb-top-left-offset-z" 2)
                        :configuration-thumb-middle-left-tenting-x (/ pi (get form "thumb-middle-left-tenting-x" 10))
                        :configuration-thumb-middle-left-tenting-y (/ pi (get form "thumb-middle-left-tenting-y" -4))
                        :configuration-thumb-middle-left-tenting-z (/ pi (get form "thumb-middle-left-tenting-z" 10))

                        :configuration-thumb-middle-left-offset-x  (get form "thumb-middle-left-offset-x" -35)
                        :configuration-thumb-middle-left-offset-y  (get form "thumb-middle-left-offset-y" -16)
                        :configuration-thumb-middle-left-offset-z  (get form "thumb-middle-left-offset-z" 2)
                        :configuration-use-hotswap?                (get form "hotswap" false)
                        :configuration-stagger?                    (get form "stagger" true)
                        :configuration-stagger-index               stagger-index
                        :configuration-stagger-middle              stagger-middle
                        :configuration-stagger-ring                stagger-ring
                        :configuration-stagger-pinky               stagger-pinky
                        :configuration-use-wide-pinky?             (get form "wide-pinky" false)
                        :configuration-z-offset                    (get form "height-offset" 4)
                        :configuration-use-wire-post?              (get form "wire-post" false)
                        :configuration-use-screw-inserts?          (get form "screw-inserts" false)
                        :configuration-web-thickness               (get form "web-thickness" 7.0)
                        :configuration-wall-thickness               (get form "wall-thickness" 3.0)

                        :configuration-show-caps?                  (get misc "keycaps" false)
                        :configuration-plate-projection?           (not (get misc "case" true))}
        ]
    c))

(defn generate-manuform [c right]
  (g/generate-case-dm c right))

(defn api-generate-lightcycle [body]
  (let [keys           (get body :keys)
        curve          (get body :curve)
        connector      (get body :connector)
        form           (get body :form)
        misc           (get body :misc)
        c              {:configuration-ncols                (get keys "columns" 5)
                        :configuration-use-numrow?          (get keys "num-row" false)
                        :configuration-use-lastrow?         (get keys "last-row" false)
                        :configuration-switch-type          (keyword (get keys "switch-type" "box"))
                        :configuration-thumb-count          (keyword (get keys "thumb-count" "two"))
                        :configuration-create-side-nub?     false
                        :configuration-use-alps?            false
                        :configuration-hide-last-pinky?     (get keys "hide-last-pinky" false)

                        :configuration-alpha                (/ pi (get curve "alpha" 12))
                        :configuration-beta                 (/ pi (get curve "beta" 36))
                        :configuration-tenting-angle        (/ pi (get curve "tenting" 12))
                        :configuration-thumb-alpha          (/ pi (get curve "thumb-alpha" 12))
                        :configuration-thumb-beta           (/ pi (get curve "thumb-beta" 36))
                        :configuration-thumb-tenting-angle  (/ pi (get curve "thumb-tenting" 12))

                        :configuration-use-external-holder? (get connector "external" false)

                        :configuration-use-hotswap?         (get form "hotswap" false)
                        :configuration-thumb-offset-x       (- 0 (get form "thumb-offset-x" 52))
                        :configuration-thumb-offset-y       (- 0 (get form "thumb-offset-y" 45))
                        :configuration-thumb-offset-z       (get form "thumb-offset-z" 27)
                        :configuration-use-wide-pinky?      (get form "wide-pinky" false)
                        :configuration-z-offset             (get form "z-offset" 10)
                        :configuration-web-thickness        (get form "web-thickness" 7)
                        :configuration-manuform-offset?     (get form "manuform-offset" false)
                        :configuration-use-border?          (get form "use-border" true)
                        :configuration-thick-wall?          (get form "thick-wall" false)

                        :configuration-use-screw-inserts?   (get misc "screw-inserts" false)}
        ]
    c))

(defn generate-lightcycle [c right]
  (g/generate-case-dl c right))
