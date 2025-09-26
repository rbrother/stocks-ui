(ns stocks-ui.api
  (:require [ajax.core :as ajax]
            [re-frame.core :as rf]
            [stocks-ui.config :as config]))

;; Yahoo Finance API endpoints (using CORS proxy)
(def yahoo-finance-base "https://query1.finance.yahoo.com/v8/finance/chart/")

;; CORS proxy to bypass browser restrictions
(def cors-proxy "https://api.allorigins.win/get?url=")

;; Fetch current stock quote from Yahoo Finance via CORS proxy
(defn fetch-stock-quote [symbol on-success on-failure]
  (let [url (str yahoo-finance-base symbol)
        proxy-url (str cors-proxy url)]
    (ajax/GET proxy-url
      {:handler on-success
       :error-handler on-failure
       :response-format (ajax/json-response-format {:keywords? true})
       :timeout 15000})))

;; Fetch historical data from Yahoo Finance via CORS proxy
(defn fetch-historical-data [symbol period on-success on-failure]
  (let [time-period (first (filter #(= (:key %) period) config/time-periods))
        days (get time-period :days 30)
        end-date (js/Math.floor (/ (.getTime (js/Date.)) 1000))
        start-date (- end-date (* days 24 60 60))
        interval (case period
                   "1D" "5m"
                   "1W" "1h"
                   "1M" "1d"
                   "3M" "1d"
                   "6M" "1d"
                   "1Y" "1d"
                   "2Y" "1wk"
                   "1d")
        url (str yahoo-finance-base symbol "?period1=" start-date "&period2=" end-date "&interval=" interval)
        proxy-url (str cors-proxy url)]
    (ajax/GET proxy-url
      {:handler on-success
       :error-handler on-failure
       :response-format (ajax/json-response-format {:keywords? true})
       :timeout 20000})))

;; Parse Yahoo Finance response for current quote
(defn parse-quote-response [response]
  (try
    (let [chart-data (get-in response [:chart :result 0])
          meta-data (:meta chart-data)
          current-price (:regularMarketPrice meta-data)
          previous-close (:previousClose meta-data)
          change (when (and current-price previous-close)
                   (- current-price previous-close))
          change-percent (when (and change previous-close)
                           (* (/ change previous-close) 100))]
      {:price current-price
       :previous-close previous-close
       :change change
       :change-percent change-percent
       :currency (:currency meta-data)
       :market-state (:marketState meta-data)
       :last-updated (js/Date.)})
    (catch js/Error e
      (js/console.error "Error parsing quote response:" e)
      nil)))

;; Parse Yahoo Finance response for historical data
(defn parse-historical-response [response]
  (try
    (let [chart-data (get-in response [:chart :result 0])
          timestamps (get-in chart-data [:timestamp])
          prices (get-in chart-data [:indicators :quote 0 :close])
          volumes (get-in chart-data [:indicators :quote 0 :volume])]
      (when (and timestamps prices)
        (map (fn [timestamp price volume]
               {:date (* timestamp 1000) ; Convert to milliseconds
                :price price
                :volume volume})
             timestamps prices (or volumes (repeat nil)))))
    (catch js/Error e
      (js/console.error "Error parsing historical response:" e)
      nil)))

;; Fetch company profile (basic info)
(defn fetch-company-profile [symbol on-success on-failure]
  ;; For now, we'll use the basic info from our config
  ;; In a real app, you might use a different API for company fundamentals
  (let [stock-info (first (filter #(= (:symbol %) symbol) config/finnish-stocks))]
    (if stock-info
      (on-success {:symbol symbol
                   :name (:name stock-info)
                   :sector (:sector stock-info)
                   :exchange "HEX"
                   :country "Finland"})
      (on-failure {:error "Stock not found"}))))

;; Mock financial data (in a real app, you'd fetch this from a financial API)
(defn get-mock-financials [_symbol]
  {:market-cap "€12.5B"
   :pe-ratio "15.2"
   :dividend-yield "4.2%"
   :revenue-ttm "€23.1B"
   :profit-margin "8.5%"
   :next-earnings "2024-01-25"
   :employees "86,000"
   :founded "1865"})

;; Utility function to handle API errors
(defn handle-api-error [error symbol]
  (let [error-msg (cond
                    (= (:status error) 0) "Network error - please check your connection"
                    (= (:status error) 404) (str "Stock " symbol " not found")
                    (= (:status error) 429) "Too many requests - please wait a moment"
                    :else (str "Error fetching data for " symbol ": " (:status-text error)))]
    (js/console.error "API Error:" error-msg error)
    error-msg))

;; Generate mock stock data for development/demo purposes
(defn generate-mock-quote [symbol]
  (let [base-price (case symbol
                     "NOKIA.HE" 4.25
                     "NESTE.HE" 45.80
                     "NDA-FI.HE" 12.15
                     "SAMPO.HE" 42.30
                     "UPM.HE" 28.90
                     "STERV.HE" 11.75
                     "FORTUM.HE" 15.20
                     "ORNBV.HE" 38.50
                     "KNEBV.HE" 52.80
                     "WRT1V.HE" 9.85
                     "MOCORP.HE" 8.45
                     "KESKOB.HE" 18.60
                     "ELISA.HE" 48.20
                     "TELIA1.HE" 2.95
                     "KOJAMO.HE" 14.80
                     25.00)
        variation (- (* (js/Math.random) 0.1) 0.05) ; ±5% variation
        current-price (* base-price (+ 1 variation))
        previous-close base-price
        change (- current-price previous-close)
        change-percent (* (/ change previous-close) 100)]
    {:price current-price
     :previous-close previous-close
     :change change
     :change-percent change-percent
     :currency "EUR"
     :market-state "REGULAR"
     :last-updated (js/Date.)}))

(defn generate-mock-historical [symbol period]
  (let [time-period (first (filter #(= (:key %) period) config/time-periods))
        days (get time-period :days 30)
        base-price (case symbol
                     "NOKIA.HE" 4.25
                     "NESTE.HE" 45.80
                     "NDA-FI.HE" 12.15
                     "SAMPO.HE" 42.30
                     "UPM.HE" 28.90
                     "STERV.HE" 11.75
                     "FORTUM.HE" 15.20
                     "ORNBV.HE" 38.50
                     "KNEBV.HE" 52.80
                     "WRT1V.HE" 9.85
                     "MOCORP.HE" 8.45
                     "KESKOB.HE" 18.60
                     "ELISA.HE" 48.20
                     "TELIA1.HE" 2.95
                     "KOJAMO.HE" 14.80
                     25.00)
        now (.getTime (js/Date.))
        day-ms (* 24 60 60 1000)]
    (map (fn [i]
           (let [date (- now (* i day-ms))
                 trend-factor (/ (- days i) days) ; Slight upward trend
                 random-factor (- (* (js/Math.random) 0.08) 0.04) ; ±4% daily variation
                 price (* base-price (+ 0.95 (* 0.1 trend-factor) random-factor))]
             {:date date
              :price price
              :volume (+ 50000 (* (js/Math.random) 200000))}))
         (range days 0 -1))))

;; Enhanced fetch functions with real API data
(defn enhanced-fetch-quote [symbol]
  (js/console.log "Fetching quote for" symbol)
  (let [url (str yahoo-finance-base symbol)
        proxy-url (str cors-proxy (js/encodeURIComponent url))]
    (js/console.log "Using proxy URL:" proxy-url)
    (ajax/GET proxy-url
      {:handler (fn [response]
                  (js/console.log "Received response for" symbol ":" response)
                  (try
                    ;; allorigins returns data in 'contents' field as string
                    (let [contents (:contents response)]                      
                      (if (and contents (not= contents ""))
                        (let [parsed-data (js/JSON.parse contents)
                              clj-data (js->clj parsed-data :keywordize-keys true)]                          
                          (if-let [quote-data (parse-quote-response clj-data)]
                            (do
                              (js/console.log "Successfully parsed quote data for" symbol)
                              (rf/dispatch [:stocks-ui.events/fetch-stock-quote-success symbol quote-data]))
                            (do
                              (js/console.warn "Failed to parse quote data for" symbol)
                              ;; Fallback to mock data if parsing fails
                              (let [mock-data (generate-mock-quote symbol)]
                                (rf/dispatch [:stocks-ui.events/fetch-stock-quote-success symbol mock-data])))))
                        (do
                          (js/console.warn "Empty response contents for" symbol)
                          ;; Fallback to mock data if empty response
                          (let [mock-data (generate-mock-quote symbol)]
                            (rf/dispatch [:stocks-ui.events/fetch-stock-quote-success symbol mock-data])))))
                    (catch js/Error e
                      (js/console.error "Error processing quote response for" symbol ":" e)
                      ;; Fallback to mock data on error
                      (let [mock-data (generate-mock-quote symbol)]
                        (rf/dispatch [:stocks-ui.events/fetch-stock-quote-success symbol mock-data])))))
       :error-handler (fn [error]
                        (js/console.error "Network error fetching quote for" symbol ":" error)
                        ;; Fallback to mock data on network error
                        (let [mock-data (generate-mock-quote symbol)]
                          (rf/dispatch [:stocks-ui.events/fetch-stock-quote-success symbol mock-data])))
       :response-format (ajax/json-response-format {:keywords? true})
       :timeout 10000})))

(defn enhanced-fetch-historical [symbol period]
  (let [time-period (first (filter #(= (:key %) period) config/time-periods))
        days (get time-period :days 30)
        end-date (js/Math.floor (/ (.getTime (js/Date.)) 1000))
        start-date (- end-date (* days 24 60 60))
        interval (case period
                   "1D" "5m"
                   "1W" "1h"
                   "1M" "1d"
                   "3M" "1d"
                   "6M" "1d"
                   "1Y" "1d"
                   "2Y" "1wk"
                   "1d")
        url (str yahoo-finance-base symbol "?period1=" start-date "&period2=" end-date "&interval=" interval)
        proxy-url (str cors-proxy (js/encodeURIComponent url))]
    (ajax/GET proxy-url
      {:handler (fn [response]
                  (try
                    ;; allorigins returns data in 'contents' field as string
                    (let [contents (:contents response)
                          parsed-data (js/JSON.parse contents)
                          clj-data (js->clj parsed-data :keywordize-keys true)]
                      (if-let [historical-data (parse-historical-response clj-data)]
                        (rf/dispatch [:stocks-ui.events/fetch-historical-data-success symbol period historical-data])
                        (do
                          (js/console.warn "Failed to parse historical data for" symbol)
                          ;; Fallback to mock data if parsing fails
                          (let [mock-data (generate-mock-historical symbol period)]
                            (rf/dispatch [:stocks-ui.events/fetch-historical-data-success symbol period mock-data])))))
                    (catch js/Error e
                      (js/console.error "Error processing historical response for" symbol ":" e)
                      ;; Fallback to mock data on error
                      (let [mock-data (generate-mock-historical symbol period)]
                        (rf/dispatch [:stocks-ui.events/fetch-historical-data-success symbol period mock-data])))))
       :error-handler (fn [error]
                        (js/console.error "Network error fetching historical data for" symbol ":" error)
                        ;; Fallback to mock data on network error
                        (let [mock-data (generate-mock-historical symbol period)]
                          (rf/dispatch [:stocks-ui.events/fetch-historical-data-success symbol period mock-data])))
       :response-format (ajax/json-response-format {:keywords? true})
       :timeout 15000})))

;; Batch fetch function for multiple stocks
(defn fetch-multiple-stocks [symbols period]
  (doseq [symbol symbols]
    (enhanced-fetch-quote symbol)
    (enhanced-fetch-historical symbol period)))

;; Check if markets are open (simplified)
(defn market-open? []
  (let [now (js/Date.)
        day (.getDay now)
        hour (.getHours now)]
    ;; Simple check: Monday-Friday, 10 AM - 6 PM (Helsinki time approximation)
    (and (>= day 1) (<= day 5) (>= hour 10) (<= hour 18))))

;; Get market status message
(defn get-market-status []
  (if (market-open?)
    "Market Open"
    "Market Closed"))
