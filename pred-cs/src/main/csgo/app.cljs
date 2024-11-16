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
       (team-popup {:source (inspect (nth @displayed-team 1))
                    :title  (nth @displayed-team 0)
                    #_#_:content [:> rn/Text (nth @displayed-team 0)]}))

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
      [:> rn/View {:style {:height :90%
                           :width (* (home-props :screen-width) 0.9)
                           :border-radius 10
                           :justify-content :center
                           :align-items :center}}
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
                            (-> props .-navigation (.navigate "Teams")))}
        "All Teams"]
       [button {:on-press (fn []
                            (-> props .-navigation (.navigate "About")))}
        "About"]]
      [:> rn/View {:style {:flex-direction :row
                           :align-items :center
                           :flex 1}}
       [:> rn/Text {:style {:font-weight :normal
                            :font-size   10
                            :color       :black}}
        "Using: shadow-cljs+expo+reagent+re-frame"]]]
     [:> StatusBar {:style "auto"}]]))

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
                    :content-fit :fit}]
      [:> rn/View  {:style {:flex 5
                            :justify-content :center
                            :flex-direction :column
                            :align-items :flex-end}}
       [:> rn/Text {:number-of-lines 1}
        (str top "° - " team)]
       [:> rn/Text {:number-of-lines 1}
        (gstring/format "%.2f" (or elo 0))]]])))

(defn- all-teams [teams-db teams-img]
  [:> rn/View
   (let [sorted-data (clj->js
                      (mapv
                       #(assoc % :id (str (str/lower-case (:team %)) "-" (random-uuid))
                               :image (teams-img (keyword (str/lower-case (:team %)))))
                       teams-db))
         render-item (fn [item]
                       (r/as-element
                        (render-team (inspect (js->clj (.-item item) :keywordize-keys true)))))]
     [:> rn/View {:style {:flex 1
                          :height :90%}}
      [:> rn/FlatList
       {:data sorted-data
        :key-extractor #(.-id %)
        :render-item render-item
        :horizontal false
        :content-container-style {:padding-horizontal 5}
        :initial-num-to-render 10
        :max-to-render-per-batch 10
        :remove-clipped-subviews true}]])])

(defn- team
  []
  (r/with-let [teams-db @(rf/subscribe [:get-teams])
               teams-img @(rf/subscribe [:get-teams-img])]
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
      (all-teams teams-db teams-img)
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

     [:> rn/ScrollView {:horizontal false
                        :showsHorizontalScrollIndicator true}
      [:> rn/View {:style {:align-items :flex-start}}
       [:> rn/Text {:style {:font-weight   :bold
                            :font-size     54
                            :color         :black
                            :margin-bottom 20}}
        (str (home-props :title))]
       [:> rn/Text {:style {:font-size     20
                            :color         :black
                            :margin-bottom 20}}
        (str "For years, e-sports, despite being in the electronic realm, did not heavily utilize game statistics and data to inform tactical and non-sporting decisions. However, recently, these data have been strongly integrated into the professional competitive scene, especially in our study subject, the Counter-Strike franchise. The volume of data generated in major competitions intuitively promotes the need to establish precise models that analyze the performance of top names in the scene. This allows us to interpret them in different contexts, whether for technical and tactical evaluations or for the actual sporting results, providing the model user with less uncertainty about outcomes that lie between the randomness of sports and the precision of statistics.

In this project, we will address the problem of how different machine learning algorithms behave in attempting to predict match outcomes, analyzing their accuracy and feasibility. The goal of this project is to compare different machine learning algorithms, initially with the idea of testing and choosing the most suitable one, in an attempt to predict the outcome of Counter-Strike matches using historical match data and average team performance. By doing so, we aim to create a system that seeks to predict results and make recommendations for probable outcomes. Given the result, the user can base decisions such as sports betting or tactical and technical decisions in the game. We will evaluate the accuracy, computational performance, and practical applicability of each algorithm in the context of e-sports predictions.")]

       [:> rn/Text {:style {:font-weight :normal
                            :font-size   15
                            :color       :blue}}
        "Built with React Native, Expo, Reagent, re-frame, and React Navigation"]]]
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
      [:> Stack.Screen {:name "Teams"
                        :component (fn [props] (r/as-element [team props]))
                        :options {:title "About"}}]]]))

(defn start
  {:dev/after-load true}
  []
  (expo-root/render-root (r/as-element [root])))

(defn init []
  (rf/dispatch-sync [:initialize-db])
  (start))
