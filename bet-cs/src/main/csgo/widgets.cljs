(ns csgo.widgets
  (:require
   ["expo-image" :refer [Image]]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [clojure.string :as str]
   ["react-native" :as rn]))

(defn button [{:keys [style text-style on-press
                      disabled? disabled-style disabled-text-style]
               :or {on-press #()}} text]
  [:> rn/Pressable {:style (cond-> {:font-weight      :bold
                                    :font-size        18
                                    :padding          6
                                    :border-radius    999
                                    :margin-bottom    20}
                             :always (merge style)
                             disabled? (merge {:background-color "#aaaaaa"}
                                              disabled-style))
                    :on-press on-press
                    :disabled disabled?}
   [:> rn/Text {:style (cond-> {:padding-left  12
                                :padding-right 12
                                :font-weight   :bold
                                :font-size     18
                                :color         :black}
                         :always (merge text-style)
                         disabled? (merge {:color :white}
                                          disabled-text-style))}
    text]])

(defn team-popup [{:keys [source title content game]
                   :or {source nil title nil content nil game false}}]
  [:> rn/View {:style {:position :absolute
                       :top 0
                       :left 0
                       :right 0
                       :bottom 0
                       :z-index 9000
                       :justify-content :center
                       :align-items :center
                       :background-color :#00000044}}
   [:> rn/View {:style {:background-color :white
                        :border-radius 10
                        :align-items :center
                        :justify-content :center
                        :margin-bottom 20
                        :width :90%
                        :height :80%}}
    [:> rn/Pressable {:on-press (fn [] (rf/dispatch [:change-game-id false]) (rf/dispatch [:change-id false]))
                      :style {:top 10
                              :height :10%
                              :right :-35%
                              :padding 10}}
     [:> rn/Text {:style {:color :black
                          :font-size 18}} "close"]]
    [:> rn/View {:style {:height :90%
                         :width :100%}}
     [:> rn/ScrollView {:horizontal false :align-items :center}
      (when source
        (if game
          (do
            [:> Image {:source (nth source 0)
                       :content-fit :cover
                       :style {:width 100
                               :height 100
                               :padding 20
                               :margin-bottom 20
                               :align-items :center}}]
            [:> Image {:source (nth source 1)
                       :content-fit :cover
                       :style {:width 100
                               :height 100
                               :padding 20
                               :margin-bottom 20
                               :align-items :center}}]))

        [:> Image {:source source
                   :content-fit :cover
                   :style {:width 100
                           :height 100
                           :padding 20
                           :margin-bottom 20
                           :align-items :center}}])
      [:> rn/Text {:style {:color :black}}
       title]
      [:> rn/View content]]]]])

(defn games-component [{:keys [content teams
                               home-props]
                        :or {content nil
                             teams [:complexity :astralis]}}]
  (let [name-team-1 (name (nth teams 0))
        name-team-2 (name (nth teams 1))
        displayed-game (rf/subscribe [:get-displayed-game])
        teams-db (rf/subscribe [:get-teams])]
    (when (and content @displayed-game)
      (rf/dispatch [:change-id false])
      (team-popup {:game true
                   :source [(get-in @teams-db [(keyword name-team-1) :image])
                            (get-in @teams-db [(keyword name-team-2) :image])]
                   :content (if (string? content)
                              [:> rn/Text content]
                              content)
                   :title (str name-team-1 "x" name-team-2)}))
    [:> rn/Pressable {:on-press #(rf/dispatch [:change-id (str "game" "-" name-team-1 "-" name-team-2)])}
     [:> rn/View {:style {:width (* 0.75 (home-props :screen-width))
                          :height 40
                          :border-radius 10
                          :background-color :white
                          :text :white
                          :flex-direction :row
                          :align-items :center
                          :justify-content :space-around
                          :margin-top 2.5
                          :margin-bottom 2.5}}
      [:> Image {:source (get-in @teams-db [(keyword name-team-1) :image])
                 :style {:width :100% :height :100%
                         :flex 3}}]
      [:> rn/Text {:style {:color :black
                           :flex 1}} ""]
      [:> Image {:source (get-in @teams-db [(keyword name-team-2) :image])
                 :style {:width :100% :height :100%
                         :flex 3}}]]]))
