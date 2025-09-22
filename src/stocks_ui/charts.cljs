(ns stocks-ui.charts
  (:require [reagent.core :as r]))

(defn create-chart-config [chart-data period]
  (let [datasets (map-indexed
                  (fn [idx [symbol data]]
                    (let [colors ["#667eea" "#764ba2" "#f093fb" "#f5576c" "#4facfe" "#00f2fe"]
                          color (nth colors (mod idx (count colors)))
                          ; Use actual date values for proper time distribution
                          chart-points (map (fn [point]
                                              {:x (:date point) :y (:price point)})
                                            data)]
                      {:label symbol
                       :data chart-points
                       :borderColor color
                       :backgroundColor (str color "20")
                       :borderWidth 2
                       :fill false
                       :tension 0.1}))
                  chart-data)]
    (clj->js
     {:type "line"
      :data {:datasets datasets}
      :options {:responsive true
                :maintainAspectRatio false
                :interaction {:mode "index"
                              :intersect false}
                :plugins {:title {:display true
                                  :text (str "Stock Prices - " period)}
                          :legend {:display true
                                   :position "top"}}
                :scales {:x {:type "linear"
                             :position "bottom"
                             :title {:display true
                                     :text "Time"}
                             :ticks {:callback (fn [value _index _values]
                                                 ; Convert timestamp to readable date
                                                 (let [date (js/Date. value)]
                                                   (.toLocaleDateString date)))}}
                         :y {:title {:display true
                                     :text "Price (EUR)"}
                             :beginAtZero false}}}})))

(defn line-chart [chart-data period]
  (let [chart-id (str "chart-" (random-uuid))
        chart-instance (atom nil)]
    (r/create-class
     {:component-did-mount
      (fn []
        (when (and (exists? js/Chart)
                   (not (empty? chart-data)))
          (when-let [canvas (.getElementById js/document chart-id)]
            (try
              (let [ctx (.getContext canvas "2d")
                    config (create-chart-config chart-data period)]
                (reset! chart-instance (js/Chart. ctx config)))
              (catch js/Error e
                (js/console.error "Error creating chart:" e))))))

      :component-will-unmount
      (fn []
        (when @chart-instance
          (.destroy @chart-instance)))

      :reagent-render
      (fn [chart-data period]
        (cond
          (not (exists? js/Chart))
          [:div.chart-error
           [:p "Chart.js is not loaded. Please refresh the page."]]

          (empty? chart-data)
          [:div.no-chart-data
           [:p "No chart data available. Select stocks and wait for data to load."]]

          :else
          [:div.chart-wrapper
           [:canvas {:id chart-id}]]))})))

(defn mini-chart [data _symbol]
  (let [chart-id (str "mini-chart-" (random-uuid))
        chart-instance (atom nil)]
    (r/create-class
     {:component-did-mount
      (fn []
        (when (and (exists? js/Chart)
                   (seq data))
          (when-let [canvas (.getElementById js/document chart-id)]
            (try
              (let [ctx (.getContext canvas "2d")
                    chart-points (map (fn [point] {:x (:date point) :y (:price point)}) data)
                    config (clj->js
                            {:type "line"
                             :data {:datasets [{:data chart-points
                                                :borderColor "#667eea"
                                                :borderWidth 1
                                                :fill false
                                                :pointRadius 0}]}
                             :options {:responsive true
                                       :maintainAspectRatio false
                                       :plugins {:legend {:display false}
                                                 :title {:display false}}
                                       :scales {:x {:type "linear"
                                                    :display false}
                                                :y {:display false
                                                    :beginAtZero false}}
                                       :elements {:point {:radius 0}}}})]
                (reset! chart-instance (js/Chart. ctx config)))
              (catch js/Error e
                (js/console.error "Error creating mini chart:" e))))))

      :component-will-unmount
      (fn []
        (when @chart-instance
          (.destroy @chart-instance)))

      :reagent-render
      (fn [_data _symbol]
        [:div.mini-chart
         [:canvas {:id chart-id
                   :style {:height "60px"}}]])})))
