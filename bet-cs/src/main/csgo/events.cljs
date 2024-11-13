(ns csgo.events
  (:require
   ["expo-file-system" :as fs]
   [re-frame.core :as rf]
   [csgo.db :as db :refer [app-db]]))

(rf/reg-event-db
 :initialize-db
 (fn [_ _]
   app-db))

(rf/reg-event-db
 :inc-counter
 (fn [db [_ _]]
   (update db :counter inc)))

(rf/reg-event-db
 :change-id
 (fn [db [_ team-id]]
   (assoc db :team-id team-id)))

(rf/reg-event-db
 :update-teams
 (fn [db [_ t]]
   (assoc db :teams t)))

(rf/reg-event-db
 :update-games
 (fn [db [_ g]]
   (assoc db :games g)))

(rf/reg-event-db
 :change-game-id
 (fn [db [_ game-id]]
   (assoc db :game-id game-id)))

(rf/reg-event-db
 :navigation/set-root-state
 (fn [db [_ navigation-root-state]]
   (assoc-in db [:navigation :root-state] navigation-root-state)))

