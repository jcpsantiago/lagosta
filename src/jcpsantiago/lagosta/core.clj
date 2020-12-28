(ns jcpsantiago.lagosta.core
  (:require 
    [jcpsantiago.lagosta.ui-components :as ui]
    [jcpsantiago.lagosta.report-fraud :as report-fraud]
    [jcpsantiago.lagosta.db :as db]
    [taoensso.timbre :refer [info]] 
    [compojure.core :refer [defroutes GET]]
    [org.httpkit.server :as server]
    [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
    [ring.middleware.params :refer [wrap-params]]
    [hiccup.page :refer [html5]])
  (:gen-class))

(defroutes all-routes
  (GET "/" [_] (ui/base-page _))
  (GET "/review-page" [_] (ui/base-page _))
  (GET "/n-review-cases" [_]
    (let [n-cases-left (some-> (db/get-n-cases-to-review db/ds)
                               first
                               :cases_left)]
      (info "Checking how many cases are left to review")
      (html5
        [:span {:id "n-cases-left-nav" 
                :class "ml-1 text-gray-400 text-sm font-medium" 
                :hx-get "/n-review-cases"
                :hx-swap "outerHTML"
                :hx-trigger "load delay:14400s"} ;4h
         (if (nil? n-cases-left) 0 n-cases-left)])))
  report-fraud/report-fraud-routes)

(defn -main
  [& _]
  (info "Starting lagosta...")
  (server/run-server 
   (-> all-routes
       (wrap-defaults site-defaults)
       wrap-params) 
   {:port 8080}))
