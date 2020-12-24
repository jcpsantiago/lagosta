(ns jcpsantiago.lagosta.core
  (:require [jcpsantiago.lagosta.db :as db]
            [jcpsantiago.lagosta.docx :as docx]
            [jcpsantiago.lagosta.gsheets :as gsheets]
            [jcpsantiago.lagosta.slack :as slack]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [resources]]
            [org.httpkit.server :as server]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [hiccup.core :refer :all]
            [hiccup.page :refer :all])
  (:gen-class))

(let [rows (db/get-cases db/db)]
  (docx/render-letter! {:rows rows})
  (slack/upload-file! "output/output.docx")
  (gsheets/append-uuids (mapv :team_name rows))
  (println "all done!"))

(defn grid-cell [& elements]
  [:div {:class "px-4 py-10 bg-white shadow-lg sm:rounded-3xl sm:p-20"}
    elements])

(defn show-landing-page [_]
  (html5
    {:class "text-gray-500 antialiased bg-white" :lang "en"}
    [:head
     (include-css "https://unpkg.com/tailwindcss@2.0.2/dist/tailwind.min.css")
     (include-js "https://unpkg.com/htmx.org@1.0.2")
     [:title "Lagosta🦞"]
     [:meta
      {:charset "utf-8",
       :name "viewport",
       :content "width=device-width, initial-scale=1.0"}]]
    [:body {:class "relative z-10 max-w-screen-lg xl:max-w-screen-xl mx-auto"}
      [:header 
         [:nav {:class "py-6 flex items-center justify-between mb-16 sm:mb-20 -mx-4 px-4 sm:mx-0 sm:px-0"}
          [:a {:href "#"} [:img {:src "/img/logo.png" :height 64}]]
          [:ul
           [:li "Menu item 1"]
           [:li "Menu item 2"]]]]
      [:main
        [:section
         [:div {:class "grid gap-4 grid-cols-3"}
          [:div {:class "col-span-1"}
           (grid-cell 
            [:form 
              (anti-forgery-field)
              [:div 
                [:label "Email"]
                [:input {:name "email" :hx-post "/contact/email" :hx-target "#current-uuids" :hx-trigger "keyup changed delay:100ms"}]]
              [:button "Submit"]])]
          [:div {:class "col-span-2"}
            (grid-cell
              [:p {:id "current-uuids" :name "current-uuids"} ""])]]]]]))

(defroutes all-routes
  (GET "/" [_] show-landing-page)
  (POST "/contact/email" req 
        (let [v (get-in req [:form-params "email"])]
          (html5
            [:p {:id "current-uuids" :name "current-uuids"} v])))

  (resources "/"))

(defn -main
  [& args]
  (println "Starting server...")
  (server/run-server 
   (-> all-routes
       (wrap-defaults site-defaults)
       wrap-params) 
   {:port 8080}))
