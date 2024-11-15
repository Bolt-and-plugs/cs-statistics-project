(ns csgo.widgets
  (:require
   [goog.string :as gstring]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [clojure.string :as str]
   ["react-native" :as rn]))

(def a (.-Animated rn))

(defn inspect [a] (prn a) a)

(defn button [{:keys [style text-style on-press
                      disabled?]
               :or {on-press #()}} text]
  (let [base-style {:font-weight :bold
                    :font-size 18
                    :padding 6
                    :border-radius 999
                    :margin-bottom 20}
        base-text-style {:padding-left 12
                         :padding-right 12
                         :font-weight :bold
                         :font-size 18
                         :color :black}
        final-style (merge base-style style (when disabled? {:background-color "#aaaaaa"}))
        final-text-style (merge base-text-style text-style (when disabled? {:color :white}))]
    [:> rn/Pressable {:style final-style
                      :on-press on-press
                      :disabled disabled?}
     [:> rn/Text {:style final-text-style} text]]))

(defn team-popup [{:keys [source title content]
                   :or {source nil title nil content nil}}]
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
    [:> rn/Pressable {:on-press (fn [] (rf/dispatch [:change-game-id false]) 
                                  (rf/dispatch [:change-id false]))
                      :style {:top 10
                              :height :10%
                              :right :-35%
                              :padding 10}}
     [:> rn/Text {:style {:color :black
                          :font-size 18}} "close"]]
    [:> rn/View {:style {:height :90%
                         :width :100%}}
     [:> rn/ScrollView {:horizontal false 
                        :align-items :center}
      [:> rn/Image {:source source
                    :content-fit :cover
                    :style {:width 100
                            :height 100
                            :padding 20
                            :margin-bottom 20
                            :align-items :center}}]
      [:> rn/Text {:style {:color :black}}
       title]
      [:> rn/View content]]]]])

(defn render-game-item [{:keys [game teams-img home-props]}]
  (let [expanded? (r/atom false)
        animation (new (.-Value a) 40)
        team1 (str/lower-case (get-in game [:team1 :name]))
        team2 (str/lower-case (get-in game [:team2 :name]))
        team1-prob  (get-in game [:team1 :win_probability])
        team2-prob  (get-in game [:team2 :win_probability])
        team1-img (teams-img (keyword team1))
        team2-img (teams-img (keyword team2))
        on-press (fn []
                   (swap! expanded? not)
                   (let [to-value (if @expanded? 120 40)]
                     (.start
                      ((.-timing a) animation
                                    #js {:toValue to-value
                                         :duration 300
                                         :useNativeDriver false}))))]
    [:> rn/Pressable {:on-press on-press}
     [:>  (.-View a) {:style {:width (* 0.8 (home-props :screen-width))
                              :height animation
                              :border-radius 10
                              :flex-direction :column
                              :align-items :center
                              :background-color :white
                              :justify-content :flex-start
                              :margin-vertical 7.5}}
      [:> rn/View {:style {:flex-direction :row
                           :align-items :center
                           :background-color :white
                           :border-radius 10
                           :height 40}}
       [:> rn/View {:style {:height 40
                            :flex 4
                            :top 0
                            :justify-content :center
                            :align-items :flex-start
                            :padding 5
                            :z-index 20}}
        [:> rn/Text {:style {:color :black
                             :font-size 10}}
         (str/upper-case (str team1 " x " team2))]
        [:> rn/Text {:style {:color :black
                             :font-size 10}}
         (str
          (gstring/format "%.2f" (* 100 team1-prob)) "%" " x " (gstring/format "%.2f" (* 100 team2-prob)) "%")]]

       [:> rn/Image {:source team1-img
                     :style {:flex 3
                             :width 50 :height 40}}]
       [:> rn/Text {:style {:margin-horizontal 10
                            :align-items :center
                            :justify-content :center
                            :height 40}}
        ""]
       [:> rn/Image {:source team2-img
                     :style {:flex 3
                             :width 50 :height 40}}]]
      (when @expanded?
        [:> rn/View {:style {:height 80
                             :width :100%
                             :border-top-left-radius 10
                             :border-top-right-radius 10
                             :background-color :#fafafa
                             :justify-content :center
                             :align-items :center}}
         [:> rn/Text {:style {:color :white}}
          "Expanded Content"]])]]))

(defn games-list [home-props]
  (let [games @(rf/subscribe [:get-games])
        teams-img @(rf/subscribe [:get-teams-img])
        render-item (fn [item]
                      (let [game (js->clj (.-item item) :keywordize-keys true)]
                        (r/as-element
                         (render-game-item {:game game
                                            :teams-img teams-img
                                            :home-props home-props}))))

        key-extractor (fn [item]
                        (let [game (js->clj item :keywordize-keys true)]
                          (str (:date game) "-"
                               (get-in game [:team1 :name]) "-"
                               (get-in game [:team2 :name]))))]

    [:> rn/View {:style {:flex 1}}
     [:> rn/FlatList
      {:data (clj->js (or games []))
       :render-item render-item
       :key-extractor key-extractor
       :style {:flex 1}
       :content-container-style {:align-items "center"
                                 :padding-vertical 7.5}
       :initial-num-to-render 8
       :window-size 5
       :max-to-render-per-batch 5
       :update-cell-batch-ingress 1
       :remove-clipped-subviews true}]]))
