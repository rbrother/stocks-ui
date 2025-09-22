(ns stocks-ui.core
  (:require [reagent.dom.client :as rdom]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [stocks-ui.events :as events]
            [stocks-ui.subs :as subs]
            [stocks-ui.views :as views]
            [stocks-ui.config :as config]))

(defonce root (atom nil))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (when-not @root
      (reset! root (rdom/create-root root-el)))
    (.render @root (r/as-element [views/main-panel]))))

(defn init! []
  (rf/dispatch-sync [::events/initialize-app])
  (dev-setup)
  (mount-root))
