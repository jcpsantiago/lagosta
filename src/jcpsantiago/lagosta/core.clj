(ns jcpsantiago.lagosta.core
  (:require 
    [jcpsantiago.lagosta.report-fraud :as report-fraud]
    [taoensso.timbre :refer [info]] 
    [compojure.core :refer [defroutes GET]]
    [org.httpkit.server :as server]
    [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
    [ring.middleware.params :refer [wrap-params]]
    [hiccup.page :refer [html5 include-css include-js]])
  (:gen-class))


(defn base-page [_]
  (html5
    {:class "" :lang "en"}
    [:head
     (include-css "https://unpkg.com/tailwindcss@2.0.2/dist/tailwind.min.css")
     (include-js "https://unpkg.com/htmx.org@1.0.2")
     [:title "Lagosta🦞"]
     [:meta
      {:charset "utf-8",
       :name "viewport",
       :content "width=device-width, initial-scale=1.0"}]]
    [:body {:class "bg-red-50"}
      [:main
        [:section {:class "mt-5 mx-5 md:mx-20"}
         [:h1 {:class "font-mono 2xl mb-8 text-indigo-500"} 
          [:a {:href "#"} "Lagosta🦞"]]
         [:div {:class "grid grid-cols-1 md:grid-cols-4 gap-5"}
          [:div {:class "md:col-span-1"}
            (report-fraud/uuids-form "")]
          [:div {:class "md:col-span-3"}
            (report-fraud/preview-report)]]]]]))

(defroutes all-routes
  (GET "/" [_] base-page)
  report-fraud/report-fraud-routes)

(defn -main
  [& _]
  (info "Starting lagosta...")
  (server/run-server 
   (-> all-routes
       (wrap-defaults site-defaults)
       wrap-params) 
   {:port 8080}))
