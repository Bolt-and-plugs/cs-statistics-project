(ns csgo.widgets
  (:require
   ["expo-image" :refer [Image]]
   [re-frame.core :as rf]
   [clojure.string :as str]
   ["react-native" :as rn]))

(defn inspect [a] (js/console.log a) a)

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
        (if (vector? source)
          (do
            [:> rn/View
             [:> Image {:source (nth (inspect source) 0)
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
                                :align-items :center}}]])
          (do [:> Image {:source source
                         :content-fit :cover
                         :style {:width 100
                                 :height 100
                                 :padding 20
                                 :margin-bottom 20
                                 :align-items :center}}])))

      [:> rn/Text {:style {:color :black}}
       title]
      [:> rn/View content]]]]])

(defn games-component [{:keys [game home-props]}]
  (let [team1     (str/lower-case (get-in game [:team1 :name]))
        team2     (str/lower-case (get-in game [:team2 :name]))
        team1-prob (get-in game [:team1 :win_probability])
        team2-prob (get-in game [:team2 :win_probability])
        displayed-game (rf/subscribe [:get-displayed-game])
        displayed-team (rf/subscribe [:get-displayed-team])
        teams-db (rf/subscribe [:get-teams])
        content (str (game :date))]

    (when (and (and (:content game) @displayed-game) (not @displayed-team))
      (rf/dispatch [:change-id false])
      (team-popup {:game game
                   :source [(get-in @teams-db [(keyword team1) :image])
                            (get-in @teams-db [(keyword team2) :image])]
                   :content (if (string? content)
                              [:> rn/Text content]
                              content)
                   :title (str team1 " x " team2)}))

    [:> rn/Pressable {:on-press #(rf/dispatch [:change-id (str "game-" team1 "-" team2)])}
     [:> rn/View {:style {:width (* 0.8 (home-props :screen-width))
                          :height 40
                          :border-radius 10
                          :background-color :white
                          :flex-direction :row
                          :align-items :center
                          :justify-content :space-around
                          :margin-top 15}}
      [:> Image {:source (get-in @teams-db [(keyword team1) :image])
                 :style {:width :100%
                         :height :100%
                         :flex 3}}]
      [:> rn/Text {:style {:flex 1}} ""]
      [:> Image {:source (get-in @teams-db [(keyword team2) :image])
                 :style {:width :100%
                         :height :100%
                         :flex 3}}]]]))
