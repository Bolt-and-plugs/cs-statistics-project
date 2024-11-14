(ns csgo.app
  (:require
   [goog.string :as gstring]
   [goog.string.format]
   [csgo.events]
   [csgo.subs]
   [csgo.db :refer [teams-img]]
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

(defn inspect [a] (js/console.log a) a)

(def render-score-item
  (memoize
   (fn [{:keys [id score]}]
     (let [image (teams-img (keyword id))]
       [:> rn/Pressable
        {:on-press #(rf/dispatch [:change-id id])
         :style {:margin-horizontal 2.5}}
        [:> rn/View {:style {:align-items "center"
                             :justify-content "space-around"
                             :margin 2.5
                             :padding 5
                             :border-radius 10
                             :width 90
                             :background-color "#fafafa"}}
         [:> rn/Image {:source image
                       :style {:width 55 :height 55}
                       :content-fit "cover"}]
         [:> rn/Text {:number-of-lines 1
                      :style {:margin-top 4}}
          (gstring/format "%.2f" (or score 0))]]]))))

(def home-props
  {:title "Bet CS"
   :home-icon ""
   :screen-width (.-width (.get (.-Dimensions rn) "window"))
   :screen-height (.-height (.get (.-Dimensions rn) "window"))})

(defonce Stack (rnn-stack/createNativeStackNavigator))

(defn home [^js props]
  (r/with-let [teams-db (rf/subscribe [:get-teams])
               displayed-team (rf/subscribe [:get-displayed-team])
               displayed-game (rf/subscribe [:get-displayed-game])
               games (rf/subscribe [:get-games])]
    [:> rn/View {:style {:padding-vertical 40
                         :flex-direction :column
                         :align-items :center
                         :justify-content :space-between
                         :height (home-props :screen-height)
                         :background-color :white}}
     (when (and @displayed-team (false? @displayed-game))
       (team-popup {:source (get-in  (inspect @teams-db) [(keyword @displayed-team) :image])
                    :title (str (get-in @teams-db [(keyword @displayed-team) :text]))}))

     [:> rn/View {:style {:flex 1}}
      [:> rn/View {:style {:height 20
                           :margin-bottom 5
                           :width (home-props :screen-width)}}
       [:> rn/Text {:style {:margin-left 10
                            :font-weight :bold}} "> Top 10"]]

      [:> rn/View
       (let [sorted-data
             (clj->js (take 10 (sort-by :score >
                                        (mapv (fn [[team-id team-data]]
                                                (assoc team-data :id (str (name team-id))))
                                              @teams-db))))
             render-item (fn [item]
                           (r/as-element
                            (render-score-item
                             (js->clj (.-item item) :keywordize-keys true))))]
         [:> rn/View {:style {:flex 1}}
          [:> rn/FlatList
           {:data sorted-data
            :key-extractor #(.-id %)
            :render-item render-item
            :horizontal true
            :style {:flex 1}
            :content-container-style {:padding-horizontal 5}
            :initial-num-to-render 4
            :window-size 3
            :max-to-render-per-batch 3
            :update-cell-batch-ingress 1
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
                            :font-weight :bold}} "> Main"]]
      [:> rn/View {:style {:height :80%
                           :width (* (home-props :screen-width) 0.9)
                           :border-radius 10
                           :justify-content :center
                           :align-items :center
                           :background-color :#fafafa}}
       [:> rn/ScrollView
        (when @games
          (doall
           (for [game @games]
             ^{:key (str (:date game) "-" (get-in game [:team1 :name]) "-" (get-in game [:team2 :name]))}
             [games-component {:game game
                               :home-props home-props}])))]]]

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
