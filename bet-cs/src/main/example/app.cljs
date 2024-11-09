(ns example.app
  (:require
   [example.events]
   [example.subs]
   [example.widgets :refer [button]]
   [expo.root :as expo-root]
   ["expo-status-bar" :refer [StatusBar]]
   [re-frame.core :as rf]
   ["react-native" :as rn]
   [reagent.core :as r]
   ["@react-navigation/native" :as rnn]
   ["@react-navigation/native-stack" :as rnn-stack]))

(def screen-width (.-width (.get (.-Dimensions rn) "window")))

(defonce Stack (rnn-stack/createNativeStackNavigator))

(defn home [^js props]
  (r/with-let [counter (rf/subscribe [:get-counter])
               tap-enabled? (rf/subscribe [:counter-tappable?])
               teams (rf/subscribe [:get-teams])]
    [:> rn/ScrollView {:vertical true
                       :style {:flex 1
                               :padding-vertical 50
                               :background-color :white}}
     [:> rn/View {:style {:height 50
                          :width screen-width}}
      [:> rn/Text {:style {:margin-left 10}} "> Top 20"]]
     [:> rn/ScrollView {:horizontal true
                        :style {:width screen-width
                                :flex 1
                                :overflow-x :auto
                                :background-color :#f3f3f3}}
      (for [[team-id {:keys [image score]}] @teams]
        ^{:key team-id}
        [:> rn/View {:style {:align-items :center :flex 1 :margin 5}}
         [:> rn/Image {:source image :style {:width 80 :height 80}}]
         [:> rn/Text (str score)]])]

     [:> rn/View {:style {:height 300
                          :padding-vertical 10
                          :width screen-width
                          :background-color :white}}
      [:> rn/View {:style {:height 20
                           :width screen-width}}
       [:> rn/Text {:style {:margin-left 10}} "> Carlinhos"]]]
     [:> rn/View {:style {:align-items :center}}
      [button {:on-press (fn []
                           (-> props .-navigation (.navigate "About")))}
       "About the project"]]
     [:> rn/View
      [:> rn/View {:style {:flex-direction :row
                           :align-items :center
                           :margin-bottom 20}}]
      [:> rn/Text {:style {:font-weight :normal
                           :font-size   15
                           :color       :blue}}
       "Using: shadow-cljs+expo+reagent+re-frame"]]
     [:> StatusBar {:style "auto"}]]))

(defn- about
  []
  (r/with-let [counter (rf/subscribe [:get-counter])]
    [:> rn/View {:style {:flex 1
                         :padding-vertical 50
                         :padding-horizontal 20
                         :justify-content :space-between
                         :align-items :flex-start
                         :background-color :white}}
     [:> rn/View {:style {:align-items :flex-start}}
      [:> rn/Text {:style {:font-weight   :bold
                           :font-size     54
                           :color         :blue
                           :margin-bottom 20}}
       "About Example App"]
      [:> rn/Text {:style {:font-weight   :bold
                           :font-size     20
                           :color         :blue
                           :margin-bottom 20}}
       (str "Counter is at: " @counter)]
      [:> rn/Text {:style {:font-weight :normal
                           :font-size   15
                           :color       :blue}}
       "Built with React Native, Expo, Reagent, re-frame, and React Navigation"]]
     [:> StatusBar {:style "auto"}]]))

(defn root []
  ;; The save and restore of the navigation root state is for development time bliss
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
                        :options {:title "Bet Cs"}}]
      [:> Stack.Screen {:name "About"
                        :component (fn [props] (r/as-element [about props]))
                        :options {:title "About"}}]]]))

(defn start
  {:dev/after-load true}
  []
  (expo-root/render-root (r/as-element [root])))

(defn init []
  (rf/dispatch-sync [:initialize-db])
  (start))
