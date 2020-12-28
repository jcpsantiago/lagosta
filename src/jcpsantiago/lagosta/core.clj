(ns jcpsantiago.lagosta.core
  (:require 
    [jcpsantiago.lagosta.report-fraud :as report-fraud]
    [jcpsantiago.lagosta.db :as db]
    [taoensso.timbre :refer [info]] 
    [compojure.core :refer [defroutes GET]]
    [org.httpkit.server :as server]
    [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
    [ring.middleware.params :refer [wrap-params]]
    [hiccup.page :refer [html5 include-css include-js]])
  (:gen-class))

(defn common-header
  "Creates the hiccup to construct the header seen in every page"
  []
  (let [nav-link-class "text-sm text-gray-800 font-semibold hover:text-indigo-500"]
    [:header {:class "mt-5 mx-5 md:mx-20 mb-10 md:mb-12"}
     [:nav {:hx-boost "true"}
      [:div {:class "flex-1 flex items-center justify-left"}
       [:h1 {:class "font-mono 2xl text-indigo-500 mr-8"} 
        [:a {:href "#"} "Lagosta🦞"]]
       [:div {:class "mr-4"}
         [:a {:href "/"
              :class nav-link-class}
          "Publish"]]
       [:div {:class "mr-4"}
         [:a {:href "/"
              :class nav-link-class}
          "Review"]
         [:span {:class "ml-1 text-gray-400 text-sm font-medium" 
                 :hx-get "/n-review-cases"
                 :hx-swap "outerHTML"
                 :hx-trigger "load"}
          ""]]]]]))


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
     (common-header)
     [:main
       [:section {:class "mt-5 mx-5 md:mx-20"}
        [:div {:class "grid grid-cols-1 md:grid-cols-4 gap-5"}
         [:div {:class "md:col-span-1"}
           (report-fraud/uuids-form "")]
         [:div {:class "md:col-span-3"}
           (report-fraud/preview-report)]]]]]))

(defroutes all-routes
  (GET "/" [_] base-page)
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
