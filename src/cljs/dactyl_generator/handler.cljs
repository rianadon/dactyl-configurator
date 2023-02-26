(ns dactyl-generator.handler
  (:require [dactyl-generator.generator :as g]
            [dactyl-generator.manuform :as dm]
            [dactyl-generator.lightcycle :as dl]
))

(def pi Math/PI)

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
        misc           (get body "misc")
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
                        :configuration-plate-projection?           (not (get misc "case" true))
                        :configuration-is-right?                   (get misc "right-side" true)
                        :configuration-is-plate?                   (get misc "plate" false)}
        ]
    c))

(defn api-generate-lightcycle [body]
  (let [keys           (get body "keys")
        curve          (get body "curve")
        connector      (get body "connector")
        form           (get body "form")
        misc           (get body "misc")
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

                        :configuration-use-screw-inserts?   (get misc "screw-inserts" false)
                        :configuration-is-right?            (get misc "right-side" true)
                        :configuration-is-plate?            (get misc "plate" false)
                        :configuration-use-case?            (get misc "use-case" true)}
        ]
    c))


(defn generate-manuform [c]
  (if (get c :configuration-is-plate?)
    (if (get c :configuration-is-right?)
      (dm/plate-right c)
      (dm/plate-left c))
    (if (get c :configuration-is-right?)
      (dm/model-right c)
      (dm/model-left c))))


(defn generate-lightcycle [c]
  (if (get c :configuration-is-plate?)
    (if (get c :configuration-is-right?)
      (dl/dactyl-plate-right c)
      (dl/dactyl-plate-left c))
    (if (get c :configuration-is-right?)
      (dl/dactyl-top-right c)
      (dl/dactyl-top-left c))))


(defn generate [config]
  (let [options (get config "options")]
    (case (get config "keyboard")
      "manuform" (generate-manuform (api-generate-manuform options))
      "lightcycle" (generate-lightcycle (api-generate-lightcycle options)))))
