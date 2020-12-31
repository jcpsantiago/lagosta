(ns jcpsantiago.lagosta.core
  (:require 
    [jcpsantiago.lagosta.ui-components :as ui]
    [jcpsantiago.lagosta.report-fraud :as report-fraud]
    [jcpsantiago.lagosta.review-fraud :as review-fraud]
    [jcpsantiago.lagosta.db :as db]
    [taoensso.timbre :refer [info]] 
    [compojure.core :refer [defroutes GET]]
    [org.httpkit.server :as server]
    [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
    [ring.middleware.params :refer [wrap-params]]
    [hiccup.page :refer [html5]]
    [clojure.core.async :refer [thread]])
  (:gen-class))

(defroutes all-routes
  (GET "/" [_] (ui/base-page
                 (ui/common-header
                   (ui/nav-link "Publish")
                   (conj (ui/nav-link "Review")
                         (ui/counter-label (:n-cases @db/db-data-holder) "/n-review-cases")))))
  (GET "/n-review-cases" [_]
    (info "Checking how many cases are left to review"
      (swap! db/db-data-holder conj {:cases (db/get-cases-to-review db/ds)})

      (info "Saving number of cases to review")
      (swap! db/db-data-holder conj {:n-cases (count (:cases @db/db-data-holder))})

      (info "Savings uuids in a separate key")
      (swap! db/db-data-holder conj {:uuids (mapv :uuid (:cases @db/db-data-holder))})

      (info "Getting order items async, saving in :order-items")
      (thread (swap! db/db-data-holder conj
                   {:order-items 
                    (reduce (fn [p c] (->> (db/get-orderitems-by-uuid db/ds {:uuid c}))
                                   (conj p))
                            []
                            (:uuids db/db-data-holder))}))
      (html5
        (ui/counter-label 
          ; FIXME the vals will be nil, but the value will always at least 1
         (:n-cases @db/db-data-holder)
         "/n-review-cases" 
         {:hx-trigger "load delay:14400s"})))) ;4h

  report-fraud/report-fraud-routes
  review-fraud/review-fraud-routes)

(defn -main
  [& _]
  (info "Starting lagosta...")

  (info "Getting cases to review and saving them in an atom")
  (swap! db/db-data-holder conj {:cases (db/get-cases-to-review db/ds)})

  (info "Saving number of cases to review")
  (swap! db/db-data-holder conj {:n-cases (count (:cases @db/db-data-holder))})

  (info "Savings uuids in a separate key")
  (swap! db/db-data-holder conj {:uuids (mapv :uuid (:cases @db/db-data-holder))})

  (info "Getting order items async, saving in :order-items")
  (thread (swap! db/db-data-holder conj
               {:order-items 
                (reduce (fn [p c] (->> (db/get-orderitems-by-uuid db/ds {:uuid c}))
                               (conj p))
                        []
                        (:uuids db/db-data-holder))}))
  (info "Starting server")
  (server/run-server 
   (-> all-routes
       (wrap-defaults site-defaults)
       wrap-params) 
   {:port 8080}))
