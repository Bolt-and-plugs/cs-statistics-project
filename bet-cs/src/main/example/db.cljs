(ns example.db
  (:require
   [clojure.string :as str]))

(def teams {:furia {:image (js/require "assets/furia.png") :score 1000}
            :navi {:image (js/require "assets/navi.png") :score 1484}
            :sk {:image (js/require "assets/sk.png") :score 1516}
            :astralis {:image (js/require "assets/astralis.png") :score 1000}
            :luminosity {:image (js/require "assets/luminosity.png") :score 1516}
            :nip {:image (js/require "assets/nip.png") :score 1516}
            :g2 {:image (js/require "assets/g2.png") :score 1516}
            :s {:image (js/require "assets/faze.svg") :score 3000}
            :hellraisers {:image (js/require "assets/hellraisers.png") :score 1516}
            :cloud9 {:image (js/require "assets/cloud9.png") :score 1484}})

(defn sort-teams-by-score [teams]
  (sort-by (comp - :score val) teams))

;; initial state of app-db
(defonce app-db {:counter 0
                 :counter-tappable? true
                 :teams (sort-teams-by-score teams)})
