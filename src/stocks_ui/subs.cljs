(ns stocks-ui.subs
  (:require [re-frame.core :as rf]
            [clojure.string :as str]))

;; Basic subscriptions
(rf/reg-sub
 ::db
 (fn [db _]
   db))

(rf/reg-sub
 ::search-term
 (fn [db _]
   (:search-term db)))

(rf/reg-sub
 ::selected-stock
 (fn [db _]
   (:selected-stock db)))

(rf/reg-sub
 ::current-time-period
 (fn [db _]
   (:current-time-period db)))

(rf/reg-sub
 ::loading?
 (fn [db _]
   (:loading? db)))

(rf/reg-sub
 ::error
 (fn [db _]
   (:error db)))

;; Filtered stocks based on search
(rf/reg-sub
 ::filtered-stocks
 (fn [db _]
   (let [search-term (str/lower-case (:search-term db))
         stocks (:available-stocks db)]
     (if (str/blank? search-term)
       stocks
       (filter (fn [stock]
                 (or (str/includes? (str/lower-case (:symbol stock)) search-term)
                     (str/includes? (str/lower-case (:name stock)) search-term)
                     (str/includes? (str/lower-case (:sector stock)) search-term)))
               stocks)))))

;; Stock data for selected stock
(rf/reg-sub
 ::selected-stock-data
 (fn [db _]
   (let [selected-stock (:selected-stock db)
         stock-data (:stock-data db)
         available-stocks (:available-stocks db)]
     (when selected-stock
       (let [stock-info (first (filter #(= (:symbol %) selected-stock) available-stocks))
             data (get stock-data selected-stock)]
         (merge stock-info data {:symbol selected-stock}))))))

;; Chart data for a specific stock
(rf/reg-sub
 ::chart-data
 (fn [db [_ symbol period]]
   (get-in db [:chart-data symbol period])))

;; Current chart data for selected stock
(rf/reg-sub
 ::current-chart-data
 (fn [db _]
   (let [selected-stock (:selected-stock db)
         period (:current-time-period db)
         chart-data (:chart-data db)]
     (when selected-stock
       (let [data (get-in chart-data [selected-stock period])]
         (when data
           {selected-stock data}))))))

;; Stock details for sidebar
(rf/reg-sub
 ::stock-details
 (fn [db [_ symbol]]
   (let [stock-data (get-in db [:stock-data symbol])
         available-stocks (:available-stocks db)
         stock-info (first (filter #(= (:symbol %) symbol) available-stocks))]
     (merge stock-info stock-data))))

;; Check if stock is selected
(rf/reg-sub
 ::stock-selected?
 (fn [db [_ symbol]]
   (= (:selected-stock db) symbol)))
