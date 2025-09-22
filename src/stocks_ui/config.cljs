(ns stocks-ui.config)

(def debug?
  ^boolean goog.DEBUG)

;; API Configuration
(def finnhub-api-key "YOUR_FINNHUB_API_KEY") ; Replace with actual API key
(def finnhub-base-url "https://finnhub.io/api/v1")

;; Finnish stock symbols (HEX exchange)
(def finnish-stocks
  [{:symbol "NOKIA.HE" :name "Nokia Oyj" :sector "Technology"}
   {:symbol "NESTE.HE" :name "Neste Oyj" :sector "Energy"}
   {:symbol "SAMPO.HE" :name "Sampo Oyj" :sector "Financial Services"}
   {:symbol "UPM.HE" :name "UPM-Kymmene Oyj" :sector "Materials"}
   {:symbol "FORTUM.HE" :name "Fortum Oyj" :sector "Utilities"}
   {:symbol "STORA.HE" :name "Stora Enso Oyj" :sector "Materials"}
   {:symbol "KESKO.HE" :name "Kesko Oyj" :sector "Consumer Discretionary"}
   {:symbol "METSO.HE" :name "Metso Outotec Oyj" :sector "Industrials"}
   {:symbol "ELISA.HE" :name "Elisa Oyj" :sector "Communication Services"}
   {:symbol "TELIA1.HE" :name "Telia Company AB" :sector "Communication Services"}
   {:symbol "NORDEA.HE" :name "Nordea Bank Abp" :sector "Financial Services"}
   {:symbol "ORION.HE" :name "Orion Oyj" :sector "Healthcare"}
   {:symbol "KOJAMO.HE" :name "Kojamo Oyj" :sector "Real Estate"}
   {:symbol "KONE.HE" :name "KONE Oyj" :sector "Industrials"}
   {:symbol "WARTSILA.HE" :name "Wärtsilä Oyj" :sector "Industrials"}])

;; Time periods for charts
(def time-periods
  [{:key "1D" :label "1 Day" :days 1}
   {:key "1W" :label "1 Week" :days 7}
   {:key "1M" :label "1 Month" :days 30}
   {:key "3M" :label "3 Months" :days 90}
   {:key "6M" :label "6 Months" :days 180}
   {:key "1Y" :label "1 Year" :days 365}
   {:key "2Y" :label "2 Years" :days 730}])
