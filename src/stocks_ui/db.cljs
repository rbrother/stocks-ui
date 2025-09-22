(ns stocks-ui.db
  (:require [stocks-ui.config :as config]))

(def default-db
  {:selected-stock nil ; Single stock selection instead of set
   :available-stocks config/finnish-stocks
   :search-term ""
   :current-time-period "1M"
   :stock-data {}
   :stock-details {}
   :loading? false
   :error nil
   :chart-data {}
   :last-updated nil})
