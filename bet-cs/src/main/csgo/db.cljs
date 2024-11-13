(ns csgo.db
  (:require
   [clojure.core.async :as async :refer [<!]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [clojure.string :as str]
   [re-frame.core :as rf]))

(def url-match "https://csgo-api-production-b5a0.up.railway.app/match?")
(def url-teams "https://csgo-api-production-b5a0.up.railway.app/rankings?")

(defn inspect [a] (js/console.log a) a)

(defonce teams-js (atom nil))

(defonce games-js (atom nil))

(defn create-teams [teams-data]
  (sort-by (comp - :score val)
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
                          :text "mousesports"}}))

(defn create-games [team1 team2 prob1 prob2]
  (swap! games-js (fn [games]
                    (conj games {:team1 team1
                                 :team2 team2
                                 :prob1 prob1
                                 :prob2 prob2}))))

(def teams (create-teams
            (when-not (nil? @teams-js)
              @teams-js)))

(add-watch teams-js :teams-loaded
           (fn [_ _ _ new-value]
             (set! teams (create-teams new-value))
             (rf/dispatch [:update-teams teams])))

(defn fetch-json [uri]
  (let [headers #js {"Content-Type" "application/json"}]
    (-> (js/fetch uri #js {:method "GET" :headers headers})
        (.then (fn [response]
                 (if (.-ok response)
                   (.json response)
                   (throw (js/Error. "Failed to fetch")))))
        (.then #(js->clj % :keywordize-keys true))
        (.catch (fn [error]
                  (js/console.error "Error fetching data:" error)
                  nil)))))

(defn get-match-by-date-and-teams [team1 team2 date]
  (async/go
    (let [uri (str url-match "date=" date
                   "&team1=" (str/upper-case (name team1))
                   "&team2=" (str/upper-case (name team2)))
          match-data (<p! (fetch-json uri))]
      ;; Assuming match-data includes probability data
      (when-let [{:keys [prob1 prob2]} match-data]
        (create-games team1 team2 prob1 prob2)))))

(defn get-teams-by-date [date]
  (async/go
    (let [teams-data (<p! (fetch-json (str url-teams "date=" date)))]
      (reset! teams-js teams-data))
    (let [match-uri (str url-match "date=" date)]
      (<p! (fetch-json match-uri)))))

(def games [(get-match-by-date-and-teams :NAVI :ASTRALIS  "2018-08-30")
            (get-match-by-date-and-teams :MOUSESPORTS :ASTRALIS  "2018-08-30")])

(add-watch games-js :games-loaded
           (fn [_ _ _ new-value]
             (set! games (create-teams new-value))
             (rf/dispatch [:update-games games])))

(get-teams-by-date "2018-08-30")

;; initial state of app-db
(defonce app-db {:counter 0
                 :counter-tappable? true
                 :teams  teams
                 :games games
                 :team-id false
                 :game-id false})
