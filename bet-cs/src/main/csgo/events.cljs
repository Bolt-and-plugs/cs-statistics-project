(ns csgo.events
  (:require
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
 :navigation/set-root-state
 (fn [db [_ navigation-root-state]]
   (assoc-in db [:navigation :root-state] navigation-root-state)))

(rf/reg-event-db
 :update-team
 (fn [db [_ team-id updates]]
   (update-in db [:teams team-id] merge updates)))
