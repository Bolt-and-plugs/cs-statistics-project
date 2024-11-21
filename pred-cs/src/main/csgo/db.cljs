(ns csgo.db
  (:require
   [clojure.core.async :as async :refer [<!]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [clojure.string :as str]
   [re-frame.core :as rf]))

(def base-url "http://192.168.15.8:4000")
(def url-match (str base-url "/match?"))
(def url-teams (str base-url "/rankings?"))
(def url-metrics (str base-url "/metrics?"))

(def date (atom "2018-08-30"))

(def teams-img
  {:navi  (js/require "assets/navi.png")
   :sk  (js/require "assets/sk.png")
   :astralis  (js/require "assets/astralis.png")
   :luminosity  (js/require "assets/luminosity.png")
   :nip  (js/require "assets/nip.png")
   :g2  (js/require "assets/g2.png")
   :faze  (js/require "assets/faze.png")
   :cloud9  (js/require "assets/cloud9.png")
   :liquid  (js/require "assets/liquid.png")
   :big  (js/require "assets/big.png")
   :fnatic  (js/require "assets/fnatic.png")
   :space_soldiers  (js/require "assets/soldiers.png")
   :immortals  (js/require "assets/immortals.png")
   :mousesports  (js/require "assets/mousesports.png")
   :north  (js/require "assets/north.png")
   :mibr  (js/require "assets/mibr.png")
   :dignitas  (js/require "assets/dignitas.png")
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

(add-watch teams-js :teams-loaded
           (fn [_ _ _ new-value]
             (rf/dispatch [:update-teams new-value])))

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
        (.then (fn [e]  (js->clj e :keywordize-keys true)))
        (.catch (fn [error]
                  (js/console.error "Error fetching data:" error)
                  nil)))))

(defn get-teams-metrics-by-date [team start end]
  (async/go
    (let [uri (str url-metrics "team=" team "&date_start=" start "&date_end=" end)
          metrics-data (<p! (fetch-json uri))]
      (rf/dispatch [:change-active-team-metrics metrics-data]))))

(defn get-match-by-date-and-teams [team1 team2 date]
  (async/go
    (let [uri (str url-match "date=" date
                   "&team1=" (str/upper-case (name team1))
                   "&team2=" (str/upper-case (name team2)))
          match-data (<p! (fetch-json uri))]
      (when-not (nil? match-data)
        (swap! games-js conj match-data)))))

(defn get-teams-by-date [date]
  (async/go
    (let [uri (str url-teams "date=" date)
          teams-data ((<p! (fetch-json  uri)) :teams)]
      (reset! teams-js teams-data))))

(defn fetch-initial-games [date]
  ;; Competitive matchups based on ELO scores
  (get-match-by-date-and-teams :ASTRALIS :NAVI date) ;; Top 1 vs Top 2
  (get-match-by-date-and-teams :FAZE :LIQUID date)   ;; Top 3 vs Top 4
  (get-match-by-date-and-teams :NIP :G2 date)       ;; Top 6 vs Top 7
  (get-match-by-date-and-teams :MOUSESPORTS :NORTH date) ;; Close ELOs
  ;; Interesting matchups between mid-ranked teams
  (get-match-by-date-and-teams :CLOUD9 :FNATIC date)
  (get-match-by-date-and-teams :ENCE :HEROIC date)
  ;; Lower-ranked matchups
  (get-match-by-date-and-teams :TYLOO :VIRTUS.PRO date)
  (get-match-by-date-and-teams :GAMBIT :AGO date))

(dotimes [_ 1]
  (get-teams-by-date @date)
  (fetch-initial-games @date))

;; initial state of app-db
(defonce app-db {:teams @teams-js
                 :teams-img teams-img
                 :games @games-js
                 :date date
                 :start-date ""
                 :end-date ""
                 :team-id false
                 :game-id false
                 :team-metrics false
                 :focused-game false})
