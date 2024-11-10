(ns csgo.db
  (:require
   [clojure.string :as str]))

(def teams
  {:furia {:image (js/require "assets/furia.png")
           :score 1660.12
           :text "FURIA Esports"}
   :navi {:image (js/require "assets/navi.png")
          :score 1649.76
          :text "Natus Vincere"}
   :sk {:image (js/require "assets/sk.png")
        :score 1573.48
        :text "SK Gaming"}
   :astralis {:image (js/require "assets/astralis.png")
              :score 1624.34
              :text "Astralis"}
   :luminosity {:image (js/require "assets/luminosity.png")
                :score 1694.29
                :text "Luminosity Gaming"}
   :nip {:image (js/require "assets/nip.png")
         :score 1516
         :text "Ninjas in Pyjamas"}
   :g2 {:image (js/require "assets/g2.png")
        :score 1580.68
        :text "G2 Esports"}
   :faze {:image (js/require "assets/faze.svg")
          :score 1562.12
          :text "FaZe Clan"}
   :cloud9 {:image (js/require "assets/cloud9.png")
            :score 1577.53
            :text "Cloud9"}
   :liquid {:image (js/require "assets/liquid.png")
            :score 1573.68
            :text "Team Liquid"}
   :big {:image (js/require "assets/big.png")
         :score 1633.28
         :text "BIG"}
   :fnatic {:image (js/require "assets/fnatic.png")
            :score 1552.47
            :text "Fnatic"}
   :soldiers {:image (js/require "assets/soldiers.png")
              :score 1540.98
              :text "Space Soldiers"}
   :immortals {:image (js/require "assets/immortals.png")
               :score 1547.08
               :text "Immortals"}
   :mousesports {:image (js/require "assets/mousesports.png")
                 :score 1583.61
                 :text "mousesports"}
   :vitality {:image (js/require "assets/vitality.png")
              :score 1580.24
              :text "Team Vitality"}
   :evil-geniuses {:image (js/require "assets/evil-geniuses.png")
                   :score 1571.73
                   :text "Evil Geniuses"}
   :complexity {:image (js/require "assets/complexity.png")
                :score 1547.47
                :text "Complexity Gaming"}
   :renegades {:image (js/require "assets/renegades.png")
               :score 1546.23
               :text "Renegades"}
   :valiance {:image (js/require "assets/valiance.svg")
              :score 1541.92
              :text "Valiance"}})

(defn sort-teams-by-score [teams]
  (into {} (sort-by (comp - :score val) teams)))

;; initial state of app-db
(defonce app-db {:counter 0
                 :counter-tappable? true
                 :teams (sort-teams-by-score teams)
                 :team-id false})
