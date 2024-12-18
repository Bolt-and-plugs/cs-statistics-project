(ns csgo.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :get-counter
 (fn [db _]
   (:counter db)))

(rf/reg-sub
 :counter-tappable?
 (fn [db _]
   (:counter-tappable? db)))

(rf/reg-sub
 :get-teams
 (fn [db _]
   (:teams db)))

(rf/reg-sub
 :get-teams-img
 (fn [db _]
   (:teams-img db)))

(rf/reg-sub
 :get-games
 (fn [db _]
   (:games db)))

(rf/reg-sub
 :get-displayed-team
 (fn [db _]
   (get db :team-id)))

(rf/reg-sub
 :get-displayed-game
 (fn [db _]
   (get db :game-id)))

(rf/reg-sub
 :get-focused-game
 (fn [db _]
   (get db :focused-game)))

(rf/reg-sub
 :get-date
 (fn [db _]
   (get db :date)))

(rf/reg-sub
 :get-start-date
 (fn [db _]
   (:start-date db ""))) 

(rf/reg-sub
 :get-end-date
 (fn [db _]
   (:end-date db ""))) 

(rf/reg-sub
 :get-active-team-metrics
 (fn [db _]
   (get db :team-metrics)))

(rf/reg-sub
 :navigation/root-state
 (fn [db _]
   (get-in db [:navigation :root-state])))

