(ns csgo.db
  (:require
   [clojure.core.async :as async :refer [<!]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [clojure.string :as str]
   [re-frame.core :as rf]))

(defn inspect [a] (js/console.log a) a)

(def url-match "https://csgo-api-production-b5a0.up.railway.app/match?")
(def url-teams "https://csgo-api-production-b5a0.up.railway.app/rankings?")

(defonce date "2018-08-30")

(defonce teams-img
  {:navi  (js/require "assets/navi.png")
   :sk  (js/require "assets/sk.png")
   :astralis  (js/require "assets/astralis.png")
   :luminosity  (js/require "assets/luminosity.png")
   :nip  (js/require "assets/nip.png")
   :g2  (js/require "assets/g2.png")
   :faze  (js/require "assets/faze.svg")
   :cloud9  (js/require "assets/cloud9.png")
   :liquid  (js/require "assets/liquid.png")
   :big  (js/require "assets/big.png")
   :fnatic  (js/require "assets/fnatic.png")
   :soldiers  (js/require "assets/soldiers.png")
   :immortals  (js/require "assets/immortals.png")
   :mousesports  (js/require "assets/mousesports.png")
   :north  (js/require "assets/north.png")
   :mibr  (js/require "assets/mibr.png")
   :dignitas  (js/require "assets/dignitas.png")
   ;:ukraine  (js/require "assets/ukraine.png")   
   :godsent  (js/require "assets/godsent.png")
   :optic  (js/require "assets/optic.png")
   :nrg  (js/require "assets/nrg.png")
   :vg.cyberzen  (js/require "assets/vg.cyberzen.png")
   :tempo_storm  (js/require "assets/tempo_storm.png")
   :penta  (js/require "assets/penta.png")
   :vega_squadron  (js/require "assets/vega_squadron.png")
   :quantum_bellator_fire  (js/require "assets/quantum_bellator_fire.png")
   :tsm  (js/require "assets/tsm.png")
   :misfits  (js/require "assets/misfits.png")
   :red_reserve  (js/require "assets/red_reserve.png")
   :ldlc  (js/require "assets/ldlc.png")
   :selfless  (js/require "assets/selfless.png")
   :hellraisers  (js/require "assets/hellraisers.png")
   :ence  (js/require "assets/ence.png")
   :agg  (js/require "assets/ag.png")
   :e_frag  (js/require "assets/e_frag.png")
   :echo_fox  (js/require "assets/echo_fox.png")
   :heroic  (js/require "assets/heroic.png")
   :ago  (js/require "assets/ago.png")
   :gambit  (js/require "assets/gambit.png")
   :tyloo  (js/require "assets/tyloo.png")
   :virtus.pro  (js/require "assets/virtus.pro.png")
   :envy  (js/require "assets/envy.png")
   :renegades  (js/require "assets/renegades.png")
   :kinguin  (js/require "assets/kinguin.png")
   :clg  (js/require "assets/clg.png")
   :flipsid3  (js/require "assets/flipsid3.png")})

(defonce teams-js (atom nil))
(defonce games-js (atom []))

(defn create-teams [teams-data]
  (let [m
        {:astralis {:image (teams-img :astralis)
                    :score (get-in teams-data [:rankings :top_1 :elo])
                    :text "Astralis"}
         :luminosity {:image (teams-img :luminosity)
                      :score (get-in teams-data [:rankings :top_2 :elo])
                      :text "Luminosity Gaming"}
         :navi {:image (teams-img :navi)
                :score (get-in teams-data [:rankings :top_3 :elo])
                :text "Natus Vincere"}
         :faze {:image (teams-img :faze)
                :score (get-in teams-data [:rankings :top_4 :elo])
                :text "FaZe Clan"}
         :liquid {:image (teams-img :liquid)
                  :score (get-in teams-data [:rankings :top_5 :elo])
                  :text "Team Liquid"}
         :sk {:image (teams-img :sk)
              :score (get-in teams-data [:rankings :top_6 :elo])
              :text "SK Gaming"}
         :nip {:image (teams-img :nip)
               :score (get-in teams-data [:rankings :top_7 :elo])
               :text "Ninjas in Pyjamas"}
         :g2 {:image (teams-img :g2)
              :score (get-in teams-data [:rankings :top_8 :elo])
              :text "G2 Esports"}
         :mousesports {:image (teams-img :mousesports)
                       :score (get-in teams-data [:rankings :top_9 :elo])
                       :text "mousesports"}
         :north {:image (teams-img :north)
                 :score (get-in teams-data [:rankings :top_10 :elo])
                 :text "North"}
         :immortals {:image (teams-img :immortals)
                     :score (get-in teams-data [:rankings :top_11 :elo])
                     :text "Immortals"}
         :mibr {:image (teams-img :mibr)
                :score (get-in teams-data [:rankings :top_12 :elo])
                :text "MIBR"}
         :dignitas {:image (teams-img :dignitas)
                    :score (get-in teams-data [:rankings :top_13 :elo])
                    :text "Dignitas"}
         :cloud9 {:image (teams-img :cloud9)
                  :score (get-in teams-data [:rankings :top_14 :elo])
                  :text "Cloud9"}
         :ukraine {:image (teams-img :ukraine)
                   :score (get-in teams-data [:rankings :top_15 :elo])
                   :text "Ukraine"}
         :space-soldiers {:image (teams-img :space-soldiers)
                          :score (get-in teams-data [:rankings :top_16 :elo])
                          :text "Space Soldiers"}
         :godsent {:image (teams-img :godsent)
                   :score (get-in teams-data [:rankings :top_17 :elo])
                   :text "GODSENT"}
         :optic {:image (teams-img :optic)
                 :score (get-in teams-data [:rankings :top_18 :elo])
                 :text "OpTic"}
         :x {:image (teams-img :x)
             :score (get-in teams-data [:rankings :top_19 :elo])
             :text "X"}
         :nrg {:image (teams-img :nrg)
               :score (get-in teams-data [:rankings :top_20 :elo])
               :text "NRG"}
         :fnatic {:image (teams-img :fnatic)
                  :score (get-in teams-data [:rankings :top_21 :elo])
                  :text "Fnatic"}
         :vg-cyberzen {:image (teams-img :vg-cyberzen)
                       :score (get-in teams-data [:rankings :top_22 :elo])
                       :text "VG.CyberZen"}
         :tempo-storm {:image (teams-img :tempo-storm)
                       :score (get-in teams-data [:rankings :top_23 :elo])
                       :text "Tempo Storm"}
         :penta {:image (teams-img :penta)
                 :score (get-in teams-data [:rankings :top_24 :elo])
                 :text "PENTA"}
         :vega-squadron {:image (teams-img :vega-squadron)
                         :score (get-in teams-data [:rankings :top_25 :elo])
                         :text "Vega Squadron"}
         :quantum-bellator-fire {:image (teams-img :quantum-bellator-fire)
                                 :score (get-in teams-data [:rankings :top_26 :elo])
                                 :text "Quantum Bellator Fire"}
         :tsm {:image (teams-img :tsm)
               :score (get-in teams-data [:rankings :top_28 :elo])
               :text "TSM"}
         :misfits {:image (teams-img :misfits)
                   :score (get-in teams-data [:rankings :top_29 :elo])
                   :text "Misfits"}
         :red-reserve {:image (teams-img :red-reserve)
                       :score (get-in teams-data [:rankings :top_30 :elo])
                       :text "Red Reserve"}
         :ldlc {:image (teams-img :ldlc)
                :score (get-in teams-data [:rankings :top_31 :elo])
                :text "LDLC"}
         :selfless {:image (teams-img :selfless)
                    :score (get-in teams-data [:rankings :top_32 :elo])
                    :text "Selfless"}
         :hellraisers {:image (teams-img :hellraisers)
                       :score (get-in teams-data [:rankings :top_33 :elo])
                       :text "HellRaisers"}
         :ence {:image (teams-img :ence)
                :score (get-in teams-data [:rankings :top_34 :elo])
                :text "ENCE"}
         :agg {:image (teams-img :agg)
               :score (get-in teams-data [:rankings :top_35 :elo])
               :text "AGG"}
         :e-frag-net {:image (teams-img :e-frag-net)
                      :score (get-in teams-data [:rankings :top_36 :elo])
                      :text "E-FRAG.net"}
         :echo-fox {:image (teams-img :echo-fox)
                    :score (get-in teams-data [:rankings :top_37 :elo])
                    :text "Echo Fox"}
         :heroic {:image (teams-img :heroic)
                  :score (get-in teams-data [:rankings :top_38 :elo])
                  :text "Heroic"}
         :ago {:image (teams-img :ago)
               :score (get-in teams-data [:rankings :top_39 :elo])
               :text "AGO"}
         :gambit {:image (teams-img :gambit)
                  :score (get-in teams-data [:rankings :top_40 :elo])
                  :text "Gambit"}
         :tyloo {:image (teams-img :tyloo)
                 :score (get-in teams-data [:rankings :top_41 :elo])
                 :text "TYLOO"}
         :virtus-pro {:image (teams-img :virtus-pro)
                      :score (get-in teams-data [:rankings :top_42 :elo])
                      :text "Virtus.Pro"}
         :envy {:image (teams-img :envy)
                :score (get-in teams-data [:rankings :top_43 :elo])
                :text "Envy"}
         :big {:image (teams-img :big)
               :score (get-in teams-data [:rankings :top_44 :elo])
               :text "BIG"}
         :renegades {:image (teams-img :renegades)
                     :score (get-in teams-data [:rankings :top_45 :elo])
                     :text "Renegades"}
         :kinguin {:image (teams-img :kinguin)
                   :score (get-in teams-data [:rankings :top_46 :elo])
                   :text "Kinguin"}
         :clg {:image (teams-img :clg)
               :score (get-in teams-data [:rankings :top_47 :elo])
               :text "CLG"}
         :flipsid3 {:image (teams-img :flipsid3)
                    :score (get-in teams-data [:rankings :top_48 :elo])
                    :text "FlipSid3"}}]
    (sort-by #((into {} (map-indexed (fn [i e] [e i]) m)) (:score %)) m)
    m))


(def teams (create-teams
            (when-not (nil? @teams-js)
              @teams-js)))

(add-watch teams-js :teams-loaded
           (fn [_ _ _ new-value]
             (set! teams (create-teams new-value))
             (rf/dispatch [:update-teams teams])))

(add-watch games-js :games-loaded
           (fn [_ _ _ new-value]
             (rf/dispatch [:update-games new-value])))

(defn fetch-json [uri]
  (let [headers #js {"Content-Type" "application/json"}]
    (-> (js/fetch uri #js {:method "GET" :headers headers})
        (.then (fn [response]
                 (if (.-ok response)
                   (.json response)
                   (throw (js/Error. "Failed to fetch")))))
        (.then (fn [e] (js->clj e :keywordize-keys true)))
        (.catch (fn [error]
                  (js/console.error "Error fetching data:" error)
                  nil)))))

(defn get-match-by-date-and-teams [team1 team2 date]
  (async/go
    (let [uri (str url-match "date=" date
                   "&team1=" (str/upper-case (name team1))
                   "&team2=" (str/upper-case (name team2)))
          match-data (<p! (fetch-json uri))]
      (swap! games-js conj match-data))))

(defn get-teams-by-date [date]
  (async/go
    (let [teams-data (<p! (fetch-json (str url-teams "date=" date)))]
      (reset! teams-js teams-data))))

(defn fetch-initial-games []
  (get-match-by-date-and-teams :NIP :G2 date)
  (get-match-by-date-and-teams :NIP :IMMORTALS date)
  (get-match-by-date-and-teams :LIQUID :IMMORTALS date)
  (get-match-by-date-and-teams :LIQUID :FNATIC date)
  (get-match-by-date-and-teams :LIQUID :ASTRALIS date)
  (get-match-by-date-and-teams :CLOUD9 :ASTRALIS date)
  (get-match-by-date-and-teams :LUMINOSITY :ASTRALIS date)
  (get-match-by-date-and-teams :NAVI :ASTRALIS date)
  (get-match-by-date-and-teams :MOUSESPORTS :ASTRALIS date))

(fetch-initial-games)

(get-teams-by-date "2018-08-30")

;; initial state of app-db
(defonce app-db {:counter 0
                 :counter-tappable? true
                 :teams teams
                 :games @games-js
                 :team-id false
                 :game-id false})
