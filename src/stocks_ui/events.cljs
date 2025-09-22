(ns stocks-ui.events
  (:require [re-frame.core :as rf]
            [stocks-ui.db :as db]
            [stocks-ui.api :as api]
            [stocks-ui.storage :as storage]))

;; Initialize database
(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   (let [base-db db/default-db
         selected-stock (if (storage/storage-available?)
                          (storage/load-selected-stock)
                          (:selected-stock base-db))
         time-period (if (storage/storage-available?)
                       (storage/load-time-period)
                       (:current-time-period base-db))]
     (-> base-db
         (assoc :selected-stock selected-stock)
         (assoc :current-time-period time-period)))))

;; Search stocks
(rf/reg-event-db
 ::set-search-term
 (fn [db [_ search-term]]
   (assoc db :search-term search-term)))

;; Select single stock (replaces multi-selection)
(rf/reg-event-fx
 ::select-stock
 (fn [{:keys [db]} [_ stock-symbol]]
   (let [current-stock (:selected-stock db)
         new-selected-stock (if (= current-stock stock-symbol) nil stock-symbol)]
     ;; Save to localStorage
     (when (storage/storage-available?)
       (storage/save-selected-stock new-selected-stock))
     {:db (assoc db :selected-stock new-selected-stock)
      :fx (when new-selected-stock
            [[:dispatch [::fetch-stock-quote new-selected-stock]]
             [:dispatch [::fetch-historical-data new-selected-stock (:current-time-period db)]]])})))

;; Set time period and fetch new data
(rf/reg-event-fx
 ::set-time-period
 (fn [{:keys [db]} [_ period]]
   ;; Save to localStorage
   (when (storage/storage-available?)
     (storage/save-time-period period))
   (let [selected-stock (:selected-stock db)]
     {:db (assoc db :current-time-period period)
      :fx (when selected-stock
            [[:dispatch [::fetch-historical-data selected-stock period]]])})))

;; Loading state
(rf/reg-event-db
 ::set-loading
 (fn [db [_ loading?]]
   (assoc db :loading? loading?)))

;; Error handling
(rf/reg-event-db
 ::set-error
 (fn [db [_ error]]
   (assoc db :error error :loading? false)))

;; Clear error
(rf/reg-event-db
 ::clear-error
 (fn [db _]
   (assoc db :error nil)))

;; Fetch stock quote
(rf/reg-event-fx
 ::fetch-stock-quote
 (fn [{:keys [db]} [_ symbol]]
   {:db (assoc db :loading? true)
    :fx [[:dispatch [::api-fetch-quote symbol]]]}))

;; API fetch quote effect
(rf/reg-event-fx
 ::api-fetch-quote
 (fn [_ [_ symbol]]
   (api/enhanced-fetch-quote symbol)
   {}))

;; Handle successful stock quote fetch
(rf/reg-event-db
 ::fetch-stock-quote-success
 (fn [db [_ symbol stock-data]]
   (-> db
       (assoc-in [:stock-data symbol] stock-data)
       (assoc :loading? false))))

;; Handle failed stock quote fetch
(rf/reg-event-db
 ::fetch-stock-quote-failure
 (fn [db [_ _ error-msg]]
   (-> db
       (assoc :error error-msg)
       (assoc :loading? false))))

;; Fetch historical data
(rf/reg-event-fx
 ::fetch-historical-data
 (fn [{:keys [db]} [_ symbol period]]
   {:db (assoc db :loading? true)
    :fx [[:dispatch [::api-fetch-historical symbol period]]]}))

;; API fetch historical effect
(rf/reg-event-fx
 ::api-fetch-historical
 (fn [_ [_ symbol period]]
   (api/enhanced-fetch-historical symbol period)
   {}))

;; Handle successful historical data fetch
(rf/reg-event-db
 ::fetch-historical-data-success
 (fn [db [_ symbol period historical-data]]
   (-> db
       (assoc-in [:chart-data symbol period] historical-data)
       (assoc :loading? false))))

;; Handle failed historical data fetch
(rf/reg-event-db
 ::fetch-historical-data-failure
 (fn [db [_ _ error-msg]]
   (-> db
       (assoc :error error-msg)
       (assoc :loading? false))))

;; Fetch data for selected stock
(rf/reg-event-fx
 ::fetch-selected-stock-data
 (fn [{:keys [db]} _]
   (let [selected-stock (:selected-stock db)
         period (:current-time-period db)]
     (when selected-stock
       {:fx [[:dispatch [::fetch-stock-quote selected-stock]]
             [:dispatch [::fetch-historical-data selected-stock period]]]}))))

;; Auto-refresh data
(rf/reg-event-fx
 ::start-auto-refresh
 (fn [_ _]
   {:fx [[:dispatch [::fetch-selected-stock-data]]
         [:dispatch-later [{:ms 300000 :dispatch [::start-auto-refresh]}]]]})) ; Refresh every 5 minutes

;; Initialize app with data loading
(rf/reg-event-fx
 ::initialize-app
 (fn [_ _]
   {:fx [[:dispatch [::initialize-db]]
         [:dispatch-later [{:ms 1000 :dispatch [::fetch-selected-stock-data]}]]
         [:dispatch-later [{:ms 2000 :dispatch [::start-auto-refresh]}]]]}))
