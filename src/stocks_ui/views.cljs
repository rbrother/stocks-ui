(ns stocks-ui.views
  (:require [re-frame.core :as rf]
            [stocks-ui.subs :as subs]
            [stocks-ui.events :as events]
            [stocks-ui.config :as config]
            [stocks-ui.charts :as charts]
            [stocks-ui.api :as api]))

(defn header []
  [:div.header
   [:h1 "Finnish Stock Monitor"
    [:span.market-status {:class (if (api/market-open?) "open" "closed")}
     (api/get-market-status)]]
   [:p "Track Finnish stocks from the Helsinki Exchange (HEX)"]])

(defn search-input []
  (let [search-term @(rf/subscribe [::subs/search-term])]
    [:input.search-input
     {:type "text"
      :placeholder "Search stocks by symbol, name, or sector..."
      :value search-term
      :on-change #(rf/dispatch [::events/set-search-term (-> % .-target .-value)])}]))

(defn stock-item [stock]
  (let [selected? @(rf/subscribe [::subs/stock-selected? (:symbol stock)])]
    [:div.stock-item
     {:class (when selected? "selected")
      :title (:symbol stock)
      :on-click #(rf/dispatch [::events/select-stock (:symbol stock)])}
     [:div.stock-name (:name stock)]]))

(defn stock-selector []
  (let [filtered-stocks @(rf/subscribe [::subs/filtered-stocks])]
    [:aside.stock-selector
     [:h2 "Stocks"]
     [search-input]
     [:div.stock-list
      (for [stock filtered-stocks]
        ^{:key (:symbol stock)}
        [stock-item stock])]]))

(defn time-controls []
  (let [current-period @(rf/subscribe [::subs/current-time-period])]
    [:div.time-controls
     (for [period config/time-periods]
       ^{:key (:key period)}
       [:button.time-btn
        {:class (when (= (:key period) current-period) "active")
         :on-click #(rf/dispatch [::events/set-time-period (:key period)])}
        (:label period)])]))

(defn price-display [price change change-percent currency]
  (when price
    [:div
     [:span.price (str (.toFixed price 2) " " (or currency "EUR"))]
     (when change
       [:span {:class (if (>= change 0) "price-positive" "price-negative")}
        " (" (if (>= change 0) "+" "") (.toFixed change 2)
        (when change-percent
          (str " / " (if (>= change-percent 0) "+" "") (.toFixed change-percent 2) "%"))
        ")"])]))

(defn selected-stock-panel []
  (let [selected-stock-data @(rf/subscribe [::subs/selected-stock-data])]
    [:div.selected-stock
     [:h3 "Selected Stock"]
     (if (nil? selected-stock-data)
       [:p "No stock selected. Choose a stock from the list above."]
       [:div.stock-summary
        [:div.stock-symbol (:symbol selected-stock-data)]
        [:div.stock-name (:name selected-stock-data)]
        [price-display (:price selected-stock-data) (:change selected-stock-data)
         (:change-percent selected-stock-data) (:currency selected-stock-data)]])]))

(defn financials-display [symbol]
  (let [financials (api/get-mock-financials symbol)]
    [:div.financials-section
     [:h4 "Financial Information"]
     [:div.detail-item
      [:span.detail-label "Market Cap"]
      [:span.detail-value (:market-cap financials)]]
     [:div.detail-item
      [:span.detail-label "P/E Ratio"]
      [:span.detail-value (:pe-ratio financials)]]
     [:div.detail-item
      [:span.detail-label "Dividend Yield"]
      [:span.detail-value (:dividend-yield financials)]]
     [:div.detail-item
      [:span.detail-label "Revenue (TTM)"]
      [:span.detail-value (:revenue-ttm financials)]]
     [:div.detail-item
      [:span.detail-label "Profit Margin"]
      [:span.detail-value (:profit-margin financials)]]
     [:div.detail-item
      [:span.detail-label "Next Earnings"]
      [:span.detail-value (:next-earnings financials)]]
     [:div.detail-item
      [:span.detail-label "Employees"]
      [:span.detail-value (:employees financials)]]
     [:div.detail-item
      [:span.detail-label "Founded"]
      [:span.detail-value (:founded financials)]]]))

(defn stock-details-panel []
  (let [selected-stock @(rf/subscribe [::subs/selected-stock])
        stock-details (when selected-stock @(rf/subscribe [::subs/stock-details selected-stock]))]
    (when stock-details
      [:div.stock-details
       [:h3 (:name stock-details)]
       [:div.detail-item
        [:span.detail-label "Symbol"]
        [:span.detail-value (:symbol stock-details)]]
       [:div.detail-item
        [:span.detail-label "Sector"]
        [:span.detail-value (:sector stock-details)]]
       [:div.detail-item
        [:span.detail-label "Current Price"]
        [:span.detail-value
         [price-display (:price stock-details) (:change stock-details)
          (:change-percent stock-details) (:currency stock-details)]]]
       (when (:previous-close stock-details)
         [:div.detail-item
          [:span.detail-label "Previous Close"]
          [:span.detail-value (str (.toFixed (:previous-close stock-details) 2) " "
                                   (or (:currency stock-details) "EUR"))]])
       (when (:last-updated stock-details)
         [:div.detail-item
          [:span.detail-label "Last Updated"]
          [:span.detail-value (.toLocaleString (:last-updated stock-details))]])

       ;; Add financials section
       [financials-display selected-stock]])))

(defn charts-section []
  (let [chart-data @(rf/subscribe [::subs/current-chart-data])
        selected-stock @(rf/subscribe [::subs/selected-stock])
        current-period @(rf/subscribe [::subs/current-time-period])]
    [:div.charts-section
     [:h2 (if selected-stock
            (str "Stock Price Chart - " selected-stock)
            "Stock Price Chart")]
     [time-controls]
     (if (or (empty? chart-data) (nil? selected-stock))
       [:p "Select a stock to view its chart"]
       [:div.chart-container
        [charts/line-chart chart-data current-period]])]))

(defn error-display []
  (let [error @(rf/subscribe [::subs/error])]
    (when error
      [:div.error-message
       [:p error]
       [:button {:on-click #(rf/dispatch [::events/clear-error])} "Dismiss"]])))

(defn loading-indicator []
  (let [loading? @(rf/subscribe [::subs/loading?])]
    (when loading?
      [:div.loading-overlay
       [:div.loading-spinner]
       [:p "Loading stock data..."]])))

(defn dashboard []
  [:div.dashboard
   [charts-section]
   [:div.sidebar
    [selected-stock-panel]
    [stock-details-panel]]])

(defn main-panel []
  [:div
   [header]
   [error-display]
   [loading-indicator]
   [:div.container
    [:div.main-layout
     [stock-selector]
     [dashboard]]]

   ;; Initialize data fetching when component mounts
   [:script {:dangerouslySetInnerHTML
             {:__html "setTimeout(function() {
                        if (window.re_frame && window.re_frame.core) {
                          window.re_frame.core.dispatch(['stocks-ui.events/fetch-selected-stock-data']);
                          window.re_frame.core.dispatch(['stocks-ui.events/start-auto-refresh']);
                        }
                      }, 1000);"}}]])
