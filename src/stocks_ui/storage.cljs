(ns stocks-ui.storage
  (:require [cljs.reader :as reader]))

(def storage-keys
  {:selected-stocks "stocks-ui-selected-stocks" ; Keep for backward compatibility
   :selected-stock "stocks-ui-selected-stock"   ; New single stock storage
   :time-period "stocks-ui-time-period"
   :preferences "stocks-ui-preferences"})

(defn get-local-storage [key]
  (when-let [item (.getItem js/localStorage key)]
    (try
      (reader/read-string item)
      (catch js/Error e
        (js/console.warn "Failed to parse localStorage item:" key e)
        nil))))

(defn set-local-storage [key value]
  (try
    (.setItem js/localStorage key (pr-str value))
    (catch js/Error e
      (js/console.warn "Failed to save to localStorage:" key e))))

(defn remove-local-storage [key]
  (.removeItem js/localStorage key))

(defn clear-all-storage []
  (doseq [key (vals storage-keys)]
    (remove-local-storage key)))

;; Specific storage functions
(defn save-selected-stocks [stocks]
  (set-local-storage (:selected-stocks storage-keys) stocks))

(defn load-selected-stocks []
  (or (get-local-storage (:selected-stocks storage-keys)) #{}))

;; Single stock storage functions
(defn save-selected-stock [stock]
  (set-local-storage (:selected-stock storage-keys) stock))

(defn load-selected-stock []
  (get-local-storage (:selected-stock storage-keys)))

(defn save-time-period [period]
  (set-local-storage (:time-period storage-keys) period))

(defn load-time-period []
  (or (get-local-storage (:time-period storage-keys)) "1M"))

(defn save-preferences [preferences]
  (set-local-storage (:preferences storage-keys) preferences))

(defn load-preferences []
  (or (get-local-storage (:preferences storage-keys)) {}))

;; Check if localStorage is available
(defn storage-available? []
  (try
    (let [test-key "__test__"]
      (.setItem js/localStorage test-key "test")
      (.removeItem js/localStorage test-key)
      true)
    (catch js/Error e
      false)))
