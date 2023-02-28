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
        index-y        (get form "staggerIndexY" 0)
        index-z        (get form "staggerIndexZ" 0)
        middle-y       (get form "staggerMiddleY" 2.8)
        middle-z       (get form "staggerMiddleZ" -6.5)
        ring-y         (get form "staggerRingY" 0)
        ring-z         (get form "staggerRingZ" 0)
        pinky-y        (get form "staggerPinkyY" -13)
        pinky-z        (get form "staggerPinkyZ" 6)
        stagger-index  [0 index-y index-z]
        stagger-middle [0 middle-y middle-z]
        stagger-ring   [0 ring-y ring-z]
        stagger-pinky  [0 pinky-y pinky-z]
        misc           (get body "misc")
        c              {:configuration-ncols                       (get keys "columns" 5)
                        :configuration-nrows                       (get keys "rows" 4)
                        :configuration-thumb-count                 (keyword (get keys "thumbCount" "six"))
                        :configuration-last-row-count              (keyword (get keys "lastRow" "two"))
                        :configuration-switch-type                 (keyword (get keys "switchType" "box"))
                        :configuration-inner-column                (keyword (get keys "innerColumn" "normie"))
                        :configuration-hide-last-pinky?            (get keys "hideLastPinky" false)

                        :configuration-alpha                       (/ pi (get curve "alpha" 12))
                        :configuration-pinky-alpha                 (/ pi (get curve "pinkyAlpha" 12))
                        :configuration-beta                        (/ pi (get curve "beta" 36))
                        :configuration-centercol                   (get curve "centercol" 4)
                        :configuration-tenting-angle               (/ pi (get curve "tenting" 15))
                        :configuration-rotate-x-angle              (/ pi (get curve "rotateX" 15))

                        :configuration-use-external-holder?        (get connector "external" false)
                        :configuration-connector-type              (keyword (get connector "type" "none"))
                        :configuration-use-promicro-usb-hole?      (get connector "microUsb" false)

                        :configuration-thumb-cluster-offset-x      (get form "thumbClusterOffsetX" 6)
                        :configuration-thumb-cluster-offset-y      (get form "thumbClusterOffsetY" -3)
                        :configuration-thumb-cluster-offset-z      (get form "thumbClusterOffsetZ" 7)
                        :configuration-custom-thumb-cluster?       (get form "customThumbCluster" false)
                        :configuration-thumb-top-right-tenting-x   (/ pi (get form "thumbTentingX" 10))
                        :configuration-thumb-top-right-tenting-y   (/ pi (get form "thumbTentingY" -4))
                        :configuration-thumb-top-right-tenting-z   (/ pi (get form "thumbTentingZ" 10))
                        :configuration-thumb-top-right-offset-x    (get form "thumbTopRightOffsetX" -15)
                        :configuration-thumb-top-right-offset-y    (get form "thumbTopRightOffsetY" -10)
                        :configuration-thumb-top-right-offset-z    (get form "thumbTopRightOffsetZ" 5)
                        :configuration-thumb-top-left-tenting-x    (/ pi (get form "thumbTopLeftTentingX" 10))
                        :configuration-thumb-top-left-tenting-y    (/ pi (get form "thumbTopLeftTentingY" -4))
                        :configuration-thumb-top-left-tenting-z    (/ pi (get form "thumbTopLeftTentingZ" 10))
                        :configuration-thumb-top-left-offset-x     (get form "thumbTopLeftOffsetX" -35)
                        :configuration-thumb-top-left-offset-y     (get form "thumbTopLeftOffsetY" -16)
                        :configuration-thumb-top-left-offset-z     (get form "thumbTopLeftOffsetZ" 2)
                        :configuration-thumb-middle-left-tenting-x (/ pi (get form "thumbMiddleLeftTentingX" 10))
                        :configuration-thumb-middle-left-tenting-y (/ pi (get form "thumbMiddleLeftTentingY" -4))
                        :configuration-thumb-middle-left-tenting-z (/ pi (get form "thumbMiddleLeftTentingZ" 10))

                        :configuration-thumb-middle-left-offset-x  (get form "thumbMiddleLeftOffsetX" -35)
                        :configuration-thumb-middle-left-offset-y  (get form "thumbMiddleLeftOffsetY" -16)
                        :configuration-thumb-middle-left-offset-z  (get form "thumbMiddleLeftOffsetZ" 2)
                        :configuration-use-hotswap?                (get form "hotswap" false)
                        :configuration-stagger?                    (get form "stagger" true)
                        :configuration-stagger-index               stagger-index
                        :configuration-stagger-middle              stagger-middle
                        :configuration-stagger-ring                stagger-ring
                        :configuration-stagger-pinky               stagger-pinky
                        :configuration-use-wide-pinky?             (get form "widePinky" false)
                        :configuration-z-offset                    (get form "heightOffset" 4)
                        :configuration-use-wire-post?              (get form "wirePost" false)
                        :configuration-use-screw-inserts?          (get form "screwInserts" false)
                        :configuration-web-thickness               (get form "webThickness" 7.0)
                        :configuration-wall-thickness               (get form "wallThickness" 3.0)

                        :configuration-show-caps?                  (get misc "keycaps" false)
                        :configuration-plate-projection?           (not (get misc "case" true))
                        :configuration-is-right?                   (get misc "rightSide" true)
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
                        :configuration-use-numrow?          (get keys "numRow" false)
                        :configuration-use-lastrow?         (get keys "lastRow" false)
                        :configuration-switch-type          (keyword (get keys "switchType" "box"))
                        :configuration-thumb-count          (keyword (get keys "thumbCount" "two"))
                        :configuration-create-side-nub?     false
                        :configuration-use-alps?            false
                        :configuration-hide-last-pinky?     (get keys "hideLastPinky" false)

                        :configuration-alpha                (/ pi (get curve "alpha" 12))
                        :configuration-beta                 (/ pi (get curve "beta" 36))
                        :configuration-tenting-angle        (/ pi (get curve "tenting" 12))
                        :configuration-thumb-alpha          (/ pi (get curve "thumbAlpha" 12))
                        :configuration-thumb-beta           (/ pi (get curve "thumbBeta" 36))
                        :configuration-thumb-tenting-angle  (/ pi (get curve "thumbTenting" 12))

                        :configuration-use-external-holder? (get connector "external" false)

                        :configuration-use-hotswap?         (get form "hotswap" false)
                        :configuration-thumb-offset-x       (- 0 (get form "thumbOffsetX" 52))
                        :configuration-thumb-offset-y       (- 0 (get form "thumbOffsetY" 45))
                        :configuration-thumb-offset-z       (get form "thumbOffsetZ" 27)
                        :configuration-use-wide-pinky?      (get form "widePinky" false)
                        :configuration-z-offset             (get form "zOffset" 10)
                        :configuration-web-thickness        (get form "webThickness" 7)
                        :configuration-manuform-offset?     (get form "manuformOffset" false)
                        :configuration-use-border?          (get form "useBorder" true)
                        :configuration-thick-wall?          (get form "thickWall" false)

                        :configuration-use-screw-inserts?   (get misc "screwInserts" false)
                        :configuration-is-right?            (get misc "rightSide" true)
                        :configuration-is-plate?            (get misc "plate" false)
                        :configuration-use-case?            (get misc "useCase" true)}
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
