(ns csgo.app
  (:require
   [goog.string :as gstring]
   [goog.string.format]
   [csgo.events]
   [csgo.subs]
   [csgo.widgets :refer [button team-popup games-list]]
   [expo.root :as expo-root]
   ["expo-status-bar" :refer [StatusBar]]
   [re-frame.core :as rf]
   ["react-native" :as rn]
   [reagent.core :as r]
   ["@react-navigation/native" :as rnn]
   ["@react-navigation/native-stack" :as rnn-stack]
   [clojure.string :as str]))

(defn inspect [a] (prn a) a)

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
                     :content-fit :fit}]
       [:> rn/Text {:number-of-lines 1}
        (gstring/format "%.2f" (or elo 0))]]])))

(def home-props
  {:title "Pred CS"
   :home-icon ""
   :screen-width (.-width (.get (.-Dimensions rn) "window"))
   :screen-height (.-height (.get (.-Dimensions rn) "window"))})

(defonce Stack (rnn-stack/createNativeStackNavigator))

(defn home [^js props]
  (r/with-let [teams-db (rf/subscribe [:get-teams])
               displayed-team (rf/subscribe [:get-displayed-team])
               displayed-game (rf/subscribe [:get-displayed-game])
               teams-img (rf/subscribe [:get-teams-img])]
    [:> rn/View {:style {:padding-vertical 40
                         :flex-direction :column
                         :align-items :center
                         :justify-content :space-between
                         :height (home-props :screen-height)
                         :background-color :white}}
     (when (and @displayed-team (false? @displayed-game))
       (team-popup {:source (get @teams-img (:name @displayed-team))
                    :title  (:name @displayed-team)
                    :content [:> rn/Text (:name @displayed-team)]}))

     [:> rn/View {:style {:flex 1}}
      [:> rn/View {:style {:height 20
                           :margin-bottom 5
                           :width (home-props :screen-width)}}
       [:> rn/Text {:style {:margin-left 10
                            :font-weight :bold}} "> Top 10"]]

      [:> rn/View
       (let [sorted-data
             (clj->js (inspect (take 10 (mapv
                                         #(assoc % :id (str (str/lower-case (:team %)) "-" (random-uuid)) :image (@teams-img (keyword (str/lower-case (:team %)))))
                                         @teams-db))))
             render-item (fn [item]
                           (r/as-element
                            (render-score-item (inspect (js->clj (.-item item) :keywordize-keys true)))))]
         [:> rn/View {:style {:flex 1}}
          [:> rn/FlatList
           {:data sorted-data
            :key-extractor #(.-id %)
            :render-item render-item
            :horizontal true
            :style {:flex 1}
            :content-container-style {:padding-horizontal 5}
            :initial-num-to-render 10
            :max-to-render-per-batch 10
            :remove-clipped-subviews true}]])]]

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
                            :font-weight :bold}} "> Games"]]
      [:> rn/View {:style {:height :80%
                           :width (* (home-props :screen-width) 0.9)
                           :border-radius 10
                           :justify-content :center
                           :align-items :center
                           :background-color :#fafafa}}
       (games-list home-props)]]

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
  (r/with-let [teams-db @(rf/subscribe [:get-teams])]
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
       (str "All teams")]
      [:> rn/Text {:style {:font-weight   :bold
                           :font-size     20
                           :color         :black
                           :margin-bottom 20}}]

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
