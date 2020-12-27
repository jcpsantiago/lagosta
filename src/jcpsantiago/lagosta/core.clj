(ns jcpsantiago.lagosta.core
  (:require [taoensso.timbre :refer [info]] 
            [jcpsantiago.lagosta.db :as db]
            [jcpsantiago.lagosta.docx :as docx]
            [jcpsantiago.lagosta.gsheets :as gsheets]
            [jcpsantiago.lagosta.slack :as slack]
            [compojure.core :refer [defroutes GET POST DELETE]]
            [org.httpkit.server :as server]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [hiccup.core :refer :all]
            [hiccup.page :refer :all]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [hiccup.table :refer [to-table1d]])
  (:gen-class))

(defn grid-cell [id & elements]
  [:div {:id id :class "px-4 py-3 rounded-md shadow bg-white"}
    elements])

(defn uuid-form [vs]
  [:form {:id "uuids-form" :name "uuids-form" :hx-post "/sendtolegal"
          :hx-target "this" :hx-swap "outerHTML"}
    (anti-forgery-field)
    [:textarea {:id "selected-uuids" :name "selected-uuids" 
                :class "font-mono text-center text-sm text-gray-900 
                       w-full px-4 py-3 rounded-md shadow bg-white 
                       focus:outline-none focus:ring focus:border-indigo-300"
                :rows 10 :cols 36 :required true :value (or vs "") 
                :placeholder "Paste UUIDs here" 
                :hx-post "/uuids" 
                :hx-target "#current-uuids" 
                :hx-trigger "keyup changed"
                :hx-indicator "#ind"}]
    [:div {:class "flex flex-row-reverse mt-1"}
      [:button {:type "submit" 
                :class "items-center px-4 py-2 border border-transparent 
                       rounded-md shadow text-sm font-medium text-white 
                       bg-indigo-600 hover:bg-indigo-700 focus:outline-none 
                       focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"}
       "Send to legal"]
      [:img {:src "/img/tail-spin.svg" :class "w-8 h-8 mx-3 htmx-indicator"}]]])

(defn show-landing-page [_]
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
         [:h1 {:class "font-mono 2xl mb-8 pl-1 text-indigo-500"} "Lagosta🦞"]
         [:div {:class "grid grid-cols-1 md:grid-cols-4 gap-5"}
          [:div {:class "md:col-span-1"}
            (uuid-form "")]
          [:div {:class "md:col-span-3"}
            (grid-cell "current-uuids-cell"
              [:div {:id "current-uuids" :name "current-uuids"
                     :class "relative py-3 px-4"}
                [:span {:id "ind" :class "htmx-indicator"} "Loading data..."]])]]]]]))

(defn snakecase->normal
  "Converts a string s in snake_case to capitalized Normal text"
  [s]
  (-> (string/replace s "_" " ")
      string/capitalize))

(defroutes all-routes
  (GET "/" [_] show-landing-page)
  (DELETE "/delete" req "")
  (POST "/uuids" req
        (info "Collecting data to show in UI")
        (let [raw-ids (get-in req [:params :selected-uuids])
              split-ids (string/split-lines raw-ids)
              coll (db/get-cases-by-uuid db/db {:ids split-ids})
              ks (keys (first coll))
              headers (->> (zipmap ks (mapv name ks)) 
                           (map (fn [[k v]] [k (snakecase->normal v)]))
                           flatten
                           (into []))
              attr-fns {:table-attrs {:class "table-auto min-w-full divide-y divide-gray-200"}
                        :thead-attrs {:class ""}
                        :tbody-attrs {:class "bg-white divide-y divide-gray-200"}
                        :data-tr-attrs {:class "px-6 py-4 whitespace-nowrap text-sm"}
                        :th-attrs {:class "px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"}
                        :data-td-attrs {:class "px-6 py-4 whitespace-nowrap"}}]
          (info "Saving data in atom")
          (reset! db/db-data-holder coll)
          (info "Sending table to UI")
          (html5 {}
                 [:div {:id "current-uuids" :name "current-uuids"
                        :class "overflow-auto relative py-3 px-4"}
                   [:span {:id "ind" :class "htmx-indicator"} "Loading data..."]
                   (to-table1d coll headers attr-fns)]))) 

  (GET "/uuid-form" _
       (html5 
         {}
         (uuid-form "")
         [:div {:id "current-uuids-cell" 
                :class "px-4 py-3 rounded-md shadow bg-white"
                :hx-swap-oob "true"}
           [:div {:id "current-uuids" :name "current-uuids"
                  :class "relative py-3 px-4"}
             [:span {:id "ind" :class "htmx-indicator"} "Loading data..."]]]))
  (POST "/sendtolegal" req
    (let [form-vals (get-in req [:params :selected-uuids])
          rows @db/db-data-holder
          file (io/file "output/output.docx")]
      (info "Request to send information to legal")
      (if (.exists file)
        (do (info "Deleting old output file") 
            (.delete file)
            (info "Rendering letter...")
            (docx/render-letter! {:rows rows}))

        (do (info "Rendering letter...") 
            (docx/render-letter! {:rows rows})))

      (info "Uploading letter to slack")
      (slack/upload-letter! file)

      (info "Saving information in googlesheets")
      (gsheets/append-uuids (mapv :team_name rows))

      (info "Clearing db atom")
      (reset! db/db-data-holder {})

      (info "Letter rendered and shared!")
      (html5 
        [:div {:id "uuids-success" 
               :class "shadow rounded-md bg-white w-full"}
         [:div {:class "flex justify-center"}
           [:svg {:xmlns "http://www.w3.org/2000/svg" 
                  :viewBox "0 0 20 20" 
                  :class "fill-current text-green-100 h-48"
                  :fill "currentColor"}
             [:path {:fill-rule "evenodd" 
                     :d "M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" 
                     :clip-rule "evenodd"}]]]
         [:button {:hx-get "/uuid-form" 
                   :hx-target "#uuids-success"
                   :hx-swap "outerHTML"
                   :class "float-right mt-3 items-center px-4 py-2 text-sm 
                          font-medium text-indigo-500 focus:ring-2 
                          focus:ring-offset-2 focus:ring-indigo-500"}
          "Send another"]]))))

(defn -main
  [& args]
  (info "Starting server...")
  (server/run-server 
   (-> all-routes
       (wrap-defaults site-defaults)
       wrap-params) 
   {:port 8080}))
