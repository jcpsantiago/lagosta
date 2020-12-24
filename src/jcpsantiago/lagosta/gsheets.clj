(ns jcpsantiago.lagosta.gsheets
  (:require [clojure.edn :as edn]
            [google-apps-clj.google-sheets-v4 :as sheets]))

(def spreadsheet-id (System/getenv "LAGOSTA_SPREADSHEET_ID"))
(def sheet-id 0) ; always use the first sheet for now
(def creds (edn/read-string (slurp "resources/google-creds.edn")))

(def service (sheets/build-service creds))

(defn make-rows
  "Creates rows for appending to googlesheets
  with the format [uuid, timestamp]"
  [uuids]
  (let [time-now (str (java.time.LocalDateTime/now))]
    (mapv (fn [x] [x time-now]) uuids)))

(defn append-uuids
  "Appends uuids to googlesheets database"
  [uuids]
  (let [rows (make-rows uuids)] 
    (sheets/append-sheet service spreadsheet-id sheet-id rows)))
