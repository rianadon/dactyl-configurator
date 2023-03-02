(ns dactyl-generator.handler
  (:require [dactyl-generator.generator :as g]
            [dactyl-generator.manuform :as dm]
            [dactyl-generator.lightcycle :as dl]
))

(defn angle-to-rad [angle] (* Math/PI (/ angle 8100)))
(defn rad-to-angle [rad] (* 8100 (/ rad Math/PI)))
(defn deg-to-angle [deg] (/ (* 8100 deg) 180))
(defn get-angle [key obj default] (angle-to-rad (get key obj (rad-to-angle default)) default))

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

                        :configuration-alpha                       (get-angle curve "alpha" (/ Math/PI 12))
                        :configuration-pinky-alpha                 (get-angle curve "pinkyAlpha" (/ Math/PI 12))
                        :configuration-beta                        (get-angle curve "beta" (/ Math/PI 36))
                        :configuration-centercol                   (get curve "centercol" 4)
                        :configuration-tenting-angle               (get-angle curve "tenting" (/ Math/PI 15))
                        :configuration-rotate-x-angle              (get-angle curve "rotateX" (/ Math/PI 15))

                        :configuration-use-external-holder?        (get connector "external" false)
                        :configuration-connector-type              (keyword (get connector "type" "none"))
                        :configuration-use-promicro-usb-hole?      (get connector "microUsb" false)

                        :configuration-thumb-cluster-offset-x      (get form "thumbClusterOffsetX" 6)
                        :configuration-thumb-cluster-offset-y      (get form "thumbClusterOffsetY" -3)
                        :configuration-thumb-cluster-offset-z      (get form "thumbClusterOffsetZ" 7)
                        :configuration-custom-thumb-cluster?       (get form "customThumbCluster" false)
                        :configuration-thumb-top-right-tenting-x   (get-angle form "thumbTentingX" (/ Math/PI 10))
                        :configuration-thumb-top-right-tenting-y   (get-angle form "thumbTentingY" (/ Math/PI -4))
                        :configuration-thumb-top-right-tenting-z   (get-angle form "thumbTentingZ" (/ Math/PI 10))
                        :configuration-thumb-top-right-offset-x    (get form "thumbTopRightOffsetX" -15)
                        :configuration-thumb-top-right-offset-y    (get form "thumbTopRightOffsetY" -10)
                        :configuration-thumb-top-right-offset-z    (get form "thumbTopRightOffsetZ" 5)
                        :configuration-thumb-top-left-tenting-x    (get-angle form "thumbTopLeftTentingX" (/ Math/PI 10))
                        :configuration-thumb-top-left-tenting-y    (get-angle form "thumbTopLeftTentingY" (/ Math/PI -4))
                        :configuration-thumb-top-left-tenting-z    (get-angle form "thumbTopLeftTentingZ" (/ Math/PI 10))
                        :configuration-thumb-top-left-offset-x     (get form "thumbTopLeftOffsetX" -35)
                        :configuration-thumb-top-left-offset-y     (get form "thumbTopLeftOffsetY" -16)
                        :configuration-thumb-top-left-offset-z     (get form "thumbTopLeftOffsetZ" 2)
                        :configuration-thumb-middle-left-tenting-x (get-angle form "thumbMiddleLeftTentingX" (/ Math/PI 10))
                        :configuration-thumb-middle-left-tenting-y (get-angle form "thumbMiddleLeftTentingY" (/ Math/PI -4))
                        :configuration-thumb-middle-left-tenting-z (get-angle form "thumbMiddleLeftTentingZ" (/ Math/PI 10))

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
                        :configuration-wall-thickness              (get form "wallThickness" 3.0)

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

                        :configuration-alpha                (get-angle curve "alpha" (/ Math/PI 12))
                        :configuration-beta                 (get-angle curve "beta" (/ Math/PI 36))
                        :configuration-tenting-angle        (get-angle curve "tenting" (/ Math/PI 12))
                        :configuration-thumb-alpha          (get-angle curve "thumbAlpha" (/ Math/PI 12))
                        :configuration-thumb-beta           (get-angle curve "thumbBeta" (/ Math/PI 36))
                        :configuration-thumb-tenting-angle  (get-angle curve "thumbTenting" (/ Math/PI 12))

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
