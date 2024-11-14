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

(defonce teams-js (atom nil))
(defonce games-js (atom []))

(defn create-teams [teams-data]
  (let [m
        {:navi {:image (js/require "assets/navi.png")
                :score (get-in teams-data [:rankings :top_3 :elo])
                :text "Natus Vincere"}
         :sk {:image (js/require "assets/sk.png")
              :score (get-in teams-data [:rankings :top_6 :elo])
              :text "SK Gaming"}
         :astralis {:image (js/require "assets/astralis.png")
                    :score (get-in teams-data [:rankings :top_1 :elo])
                    :text "Astralis"}
         :luminosity {:image (js/require "assets/luminosity.png")
                      :score (get-in teams-data [:rankings :top_2 :elo])
                      :text "Luminosity Gaming"}
         :nip {:image (js/require "assets/nip.png")
               :score (get-in teams-data [:rankings :top_7 :elo])
               :text "Ninjas in Pyjamas"}
         :g2 {:image (js/require "assets/g2.png")
              :score (get-in teams-data [:rankings :top_8 :elo])
              :text "G2 Esports"}
         :faze {:image (js/require "assets/faze.svg")
                :score (get-in teams-data [:rankings :top_4 :elo])
                :text "FaZe Clan"}
         :cloud9 {:image (js/require "assets/cloud9.png")
                  :score (get-in teams-data [:rankings :top_14 :elo])
                  :text "Cloud9"}
         :liquid {:image (js/require "assets/liquid.png")
                  :score (get-in teams-data [:rankings :top_5 :elo])
                  :text "Team Liquid"}
         :big {:image (js/require "assets/big.png")
               :score (get-in teams-data [:rankings :top_44 :elo])
               :text "BIG"}
         :fnatic {:image (js/require "assets/fnatic.png")
                  :score (get-in teams-data [:rankings :top_21 :elo])
                  :text "Fnatic"}
         :soldiers {:image (js/require "assets/soldiers.png")
                    :score (get-in teams-data [:rankings :top_16 :elo])
                    :text "Space Soldiers"}
         :immortals {:image (js/require "assets/immortals.png")
                     :score (get-in teams-data [:rankings :top_11 :elo])
                     :text "Immortals"}
         :mousesports {:image (js/require "assets/mousesports.png")
                       :score (get-in teams-data [:rankings :top_9 :elo])
                       :text "mousesports"}}]
    (inspect (sort-by #((into {} (map-indexed (fn [i e] [e i]) m)) (:score %)) m))
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
