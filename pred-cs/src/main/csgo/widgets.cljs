(ns csgo.widgets
  (:require
   [goog.string :as gstring]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [clojure.string :as str]
   [csgo.db :refer [get-teams-metrics-by-date]]
   ["react-native-svg-charts" :refer [BarChart]]
   ["react-native-svg" :refer [G Text]]
   ["react-native" :as rn]))

(def a (.-Animated rn))

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

(defn team-popup [{:keys [source title content]}]
  (let [start-date (r/atom "")
        end-date (r/atom "")
        data (rf/subscribe [:get-active-team-metrics])]
    [:> rn/View {:style {:position :absolute
                         :top 0
                         :left 0
                         :right 0
                         :bottom 0
                         :z-index 90
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
      [:> rn/Pressable {:on-press (fn []
                                    (rf/dispatch [:change-game-id false])
                                    (rf/dispatch [:change-id false]))
                        :style {:top 10
                                :height :10%
                                :right :-35%
                                :padding 10}}
       [:> rn/Text {:style {:color :black
                            :font-size 18}} "close"]]
      [:> rn/View {:style {:flex 1
                           :flex-direction :row
                           :justify-content :space-around
                           :align-items :center
                           :width :90%}}
       [:> rn/Image {:source source
                     :content-fit :contain
                     :style {:width 100
                             :height 100
                             :padding 20
                             :margin-bottom 20
                             :align-items :center}}]
       [:> rn/Text {:style {:color :black :font-size 40}}
        title]]
      [:> rn/View {:style {:flex 3
                           :width :100%}}
       [:> rn/ScrollView {:horizontal false
                          :height :60%
                          :align-items :center}

        [:> rn/View {:style {:width :90%
                             :justify-content :space-around
                             :flex-direction :row
                             :align-items :center}}

         [:> rn/TextInput {:placeholder "Start Date"
                           :style {:border-color :gray
                                   :border-width 1
                                   :flex 3
                                   :padding 9
                                   :margin 5}
                           :on-change-text #(reset! start-date %)}]

         [:> rn/TextInput {:placeholder "End Date"
                           :style {:border-color :gray
                                   :flex 3
                                   :border-width 1
                                   :padding 9
                                   :margin 5}
                           :on-change-text #(reset! end-date %)}]
         [:> rn/Pressable {:on-press (fn []
                                       (get-teams-metrics-by-date title @start-date @end-date))
                           :style {:background-color :blue
                                   :padding 10
                                   :flex 1
                                   :justify-content :center
                                   :align-items :center}}
          [:> rn/Text {:style {:color :white}} "->"]]]
        [:> rn/View content]
        (if-not (and @data (= (:team @data) title))
          [:> rn/Text (str "Loading..")]
          (do
            [:> rn/View {:styles {:margin 20}}
             [:> rn/Text (str "n: " (get-in @data [:metrics :n]))]
             [:> rn/Text (str "media: " (get-in @data [:metrics :media]))]
             [:> rn/Text (str "moda: " (get-in @data [:metrics :moda]))]
             [:> rn/Text (str "mediana: " (get-in @data [:metrics :mediana]))]
             [:> rn/Text (str "dp: " (get-in @data [:metrics :dp]))]
             [:> rn/Text (str "cv: " (get-in @data [:metrics :cv]))]
             [:> rn/Text (str "minimo: " (get-in @data [:metrics :minimo]))]
             [:> rn/Text (str "q1: " (get-in @data [:metrics :q1]))]
             [:> rn/Text (str "q3: " (get-in @data [:metrics :q3]))]
             [:> rn/Text (str "maximo: " (get-in @data [:metrics :maximo]))]
             [:> rn/Text (str "iq: " (get-in @data [:metrics :iq]))]
             [:> rn/Text (str "amplitude: " (get-in @data [:metrics :amplitude]))]]))]]]]))

(defn render-game-item [{:keys [game teams-img home-props]}]
  (r/with-let [expanded? (r/atom false)
               animation (new (.-Value a) 60)
               team1 (str/lower-case (get-in game [:team1 :name]))
               team2 (str/lower-case (get-in game [:team2 :name]))
               team1-prob  (get-in game [:team1 :win_probability])
               team2-prob  (get-in game [:team2 :win_probability])
               team1-img (teams-img (keyword team1))
               team2-img (teams-img (keyword team2))
               on-press (fn []
                          (swap! expanded? not)
                          (let [to-value (if @expanded? 120 60)]
                            (.start
                             ((.-timing a) animation
                                           #js {:toValue to-value
                                                :duration 300
                                                :useNativeDriver false}))))]
    [:> rn/Pressable {:on-press on-press}
     [:>  (.-View a) {:style {:width (* 0.8 (home-props :screen-width))
                              :height 60
                              :border-radius 10
                              :flex-direction :column
                              :align-items :center
                              :background-color :#fafafa
                              :border :2px
                              :justify-content :flex-start
                              :margin-vertical 7.5}}
      [:> rn/View {:style {:flex-direction :row
                           :align-items :center
                           :background-color :#fafafa
                           :border-radius 10
                           :height 60}}
       [:> rn/View {:style {:height 60
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
                             :width 50
                             :height 60}}]
       [:> rn/Text {:style {:margin-horizontal 10
                            :align-items :center
                            :justify-content :center
                            :height 40}}
        ""]
       [:> rn/Image {:source team2-img
                     :style {:flex 3
                             :width 50 :height 60}}]]
      (when  @expanded?
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

(def render-score-item
  (memoize
   (fn [{:keys [team elo image]}]
     [:> rn/Pressable
      {:on-press #(rf/dispatch [:change-id [team image]])
       :style {:margin-horizontal 2.5}}
      [:> rn/View {:style {:align-items :center
                           :justify-content :space-around
                           :margin 2.5
                           :padding 5
                           :border-radius 10
                           :width 90
                           :background-color :#fafafa}}
       [:> rn/Image {:source image
                     :style {:width 50 :height 50}
                     :content-fit :contain}]
       [:> rn/Text {:number-of-lines 1}
        (gstring/format "%.2f" (or elo 0))]]])))

(def render-team
  (memoize
   (fn [{:keys [team elo top image]}]
     [:> rn/View {:style {:align-items :center
                          :flex-direction :row
                          :justify-content :space-around
                          :margin 2.5
                          :padding 5
                          :border-radius 10
                          :width :100%
                          :height 80
                          :background-color :#fafafa}}
      [:> rn/Image {:source image
                    :flex 1
                    :style {:width 50 :height 50}
                    :content-fit :cover}]
      [:> rn/View  {:style {:flex 5
                            :justify-content :center
                            :flex-direction :column
                            :align-items :flex-end}}
       [:> rn/Text {:number-of-lines 1}
        (str top "° - " team)]
       [:> rn/Text {:number-of-lines 1}
        (gstring/format "%.2f" (or elo 0))]]])))

(defn all-teams [teams-db teams-img]
  (let [sorted-data (clj->js
                     (mapv
                      #(assoc % :id (str (str/lower-case (:team %)) "-" (random-uuid))
                              :image (teams-img (keyword (str/replace (str/lower-case (:team %)) " " "_"))))
                      teams-db))
        render-item (fn [item]
                      (r/as-element
                       (render-team  (js->clj (.-item item) :keywordize-keys true))))]
    [:> rn/View {:style {:flex 1
                         :height :90%
                         :width :90%}}
     [:> rn/FlatList
      {:data sorted-data
       :key-extractor #(.-id %)
       :render-item render-item
       :horizontal false
       :content-container-style {:padding-horizontal 5}
       :initial-num-to-render 10
       :max-to-render-per-batch 10
       :remove-clipped-subviews true}]]))

