(ns csgo.app
  (:require
   [goog.string :as gstring]
   [goog.string.format]
   [csgo.events]
   [csgo.subs]
   [csgo.db :refer [get-match-by-date-and-teams]]
   [csgo.widgets :refer [button team-popup games-component]]
   [expo.root :as expo-root]
   ["expo-file-system" :as fs]
   ["expo-status-bar" :refer [StatusBar]]
   [re-frame.core :as rf]
   ["react-native" :as rn]
   [reagent.core :as r]
   ["@react-navigation/native" :as rnn]
   ["expo-image" :refer [Image]]
   ["@react-navigation/native-stack" :as rnn-stack]
   [clojure.string :as str]))

(def home-props
  {:title "Bet CS"
   :home-icon ""
   :screen-width (.-width (.get (.-Dimensions rn) "window"))
   :screen-height (.-height (.get (.-Dimensions rn) "window"))})

(defonce Stack (rnn-stack/createNativeStackNavigator))

(defn home [^js props]
  (r/with-let [teams (rf/subscribe [:get-teams])
               displayed-team (rf/subscribe [:get-displayed-team])
               displayed-game (rf/subscribe [:get-displayed-game])
               games (rf/subscribe [:get-games])]
    [:> rn/View {:style {:padding-vertical 40
                         :flex-direction :column
                         :justify-content :space-between
                         :height (home-props :screen-height)
                         :background-color :white}}
     (when (and @displayed-team (false? @displayed-game))
       (team-popup {:source (get-in  @teams [(keyword @displayed-team) :image])
                    :title (str (get-in @teams [(keyword @displayed-team) :text]))}))

     [:> rn/View {:style {:flex 1}}
      [:> rn/View {:style {:height 20
                           :margin-bottom 5
                           :width (home-props :screen-width)}}
       [:> rn/Text {:style {:margin-left 10
                            :font-weight :bold}} "> Top 10"]]
      [:> rn/ScrollView {:horizontal true
                         :showsHorizontalScrollIndicator false
                         :style {:width (home-props :screen-width)
                                 :flex 2
                                 :overflow-x :none}}
       (for [[team-id {:keys [image score]}] (take 10 @teams)]
         ^{:key team-id}
         [:> rn/Pressable {:on-press #(rf/dispatch [:change-id (name team-id)])}
          [:> rn/View {:style {:align-items :center
                               :justify-content :space-around
                               :flex 1
                               :margin 5
                               :padding 5
                               :border-radius 10
                               :width 90
                               :background-color "#fafafa"}}
           [:> Image {:source image
                      :content-fit :cover
                      :style {:width :60%
                              :height :60%}}]
           [:> rn/Text (gstring/format "%.2f" (if score score 0))]]])]]

     [:> rn/View {:style {:flex 4
                          :padding-vertical 30
                          :width (home-props :screen-width)
                          :align-items :center
                          :background-color :white
                          :border-radius 10}}
      [:> rn/View {:style {:height 20
                           :margin-bottom 4
                           :width (home-props :screen-width)}}
       [:> rn/Text {:style {:margin-left 10
                            :font-weight :bold}} "> Main"]]
      [:> rn/View {:style {:height :80%
                           :width (* (home-props :screen-width) 0.9)
                           :border-radius 10
                           :background-color :#fafafa}}
       [:> rn/ScrollView {:horizontal false
                          :align-items :center
                          :width (* 0.95 (home-props :screen-width))
                          :justify-content :center}
        (doseq [[team1 team2] @games]
          (games-component {:teams [(keyword team1) (keyword team2)]
                            :home-props home-props :teams-db teams}))]]]

     [:> rn/View {:background-color :#fafafa
                  :flex 1
                  :align-items :center
                  :flex-direction :column
                  :justify-content :space-around}
      [:> rn/ScrollView {:horizontal true
                         :showsHorizontalScrollIndicator false
                         :style {:width (home-props :screen-width)
                                 :flex 1}}
       [button {:on-press (fn []
                            (-> props .-navigation (.navigate "Home")))}
        "Home"]
       [button {:on-press (fn []
                            (-> props .-navigation (.navigate "About")))}
        "About"]
       [button {:on-press (fn []
                            (-> props .-navigation (.navigate "Team")))}
        "Team"]]
      [:> rn/View {:style {:flex-direction :row
                           :align-items :center
                           :flex 1}}
       [:> rn/Text {:style {:font-weight :normal
                            :font-size   10
                            :color       :black}}
        "Using: shadow-cljs+expo+reagent+re-frame"]]]
     [:> StatusBar {:style "auto"}]]))

(defn- team
  []
  (r/with-let []
    [:> rn/View {:style {:flex 1
                         :padding-vertical 50
                         :padding-horizontal 20
                         :justify-content :space-between
                         :align-items :flex-start
                         :background-color :white}}
     [:> rn/View {:style {:align-items :flex-start}}
      [:> rn/Text {:style {:font-weight   :bold
                           :font-size     54
                           :color         :black
                           :margin-bottom 20}}
       (str "Our Team")]
      [:> rn/Text {:style {:font-weight   :bold
                           :font-size     20
                           :color         :black
                           :margin-bottom 20}}
       (str "This app is...")]
      [:> rn/Text {:style {:font-weight :normal
                           :font-size   15
                           :color       :blue}}
       "Built with React Native, Expo, Reagent, re-frame, and React Navigation"]]
     [:> StatusBar {:style "auto"}]]))

(defn- about
  []
  (r/with-let []
    [:> rn/View {:style {:flex 1
                         :padding-vertical 50
                         :padding-horizontal 20
                         :justify-content :space-between
                         :align-items :flex-start
                         :background-color :white}}
     [:> rn/View {:style {:align-items :flex-start}}
      [:> rn/Text {:style {:font-weight   :bold
                           :font-size     54
                           :color         :black
                           :margin-bottom 20}}
       (str "About " (home-props :title))]
      [:> rn/Text {:style {:font-weight   :bold
                           :font-size     20
                           :color         :black
                           :margin-bottom 20}}
       (str "This app is...")]
      [:> rn/Text {:style {:font-weight :normal
                           :font-size   15
                           :color       :blue}}
       "Built with React Native, Expo, Reagent, re-frame, and React Navigation"]]
     [:> StatusBar {:style "auto"}]]))

(defn root []
  (r/with-let [!root-state (rf/subscribe [:navigation/root-state])
               save-root-state! (fn [^js state]
                                  (rf/dispatch [:navigation/set-root-state state]))
               add-listener! (fn [^js navigation-ref]
                               (when navigation-ref
                                 (.addListener navigation-ref "state" save-root-state!)))]
    [:> rnn/NavigationContainer {:ref add-listener!
                                 :initialState (when @!root-state (-> @!root-state .-data .-state))}
     [:> Stack.Navigator
      [:> Stack.Screen {:name "Home"
                        :component (fn [props] (r/as-element [home props]))
                        :options {:title (home-props :title)}}]
      [:> Stack.Screen {:name "About"
                        :component (fn [props] (r/as-element [about props]))
                        :options {:title "About"}}]
      [:> Stack.Screen {:name "Team"
                        :component (fn [props] (r/as-element [team props]))
                        :options {:title "About"}}]]]))

(defn start
  {:dev/after-load true}
  []
  (expo-root/render-root (r/as-element [root])))

(defn init []
  (rf/dispatch-sync [:initialize-db])
  (start))
