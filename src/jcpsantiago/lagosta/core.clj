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
    [hiccup.page :refer [html5]])
  (:gen-class))

(defroutes all-routes
  (GET "/" [_] (ui/base-page
                 (ui/common-header
                   (ui/nav-link "Publish")
                   (conj (ui/nav-link "Review")
                         (ui/counter-label "/n-review-cases")))))
  (GET "/n-review-cases" [_]
    (let [n-cases-left (some-> (db/get-n-cases-to-review db/ds)
                               first
                               :cases_left)]
      (info "Checking how many cases are left to review")
      (html5
        (ui/counter-label 
         (if (nil? n-cases-left) 0 n-cases-left)
         "/n-review-cases" 
         {:hx-trigger "load delay:14400s"})))) ;4h

  report-fraud/report-fraud-routes
  review-fraud/review-fraud-routes)

(defn -main
  [& _]
  (info "Starting lagosta...")
  (server/run-server 
   (-> all-routes
       (wrap-defaults site-defaults)
       wrap-params) 
   {:port 8080}))
