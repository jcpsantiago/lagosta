(ns jcpsantiago.lagosta.report-fraud
  "This namespace has the UI and backend for the module to 
  report fraud cases to the legal department."
  (:require [jcpsantiago.lagosta.ui-components :as ui]
            [jcpsantiago.lagosta.db :as db]
            [jcpsantiago.lagosta.strings :as jstrs]
            [jcpsantiago.lagosta.docx :as docx]
            [jcpsantiago.lagosta.gsheets :as gsheets]
            [jcpsantiago.lagosta.slack :as slack]
            [taoensso.timbre :refer [info]] 
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [hiccup.page :refer [html5]]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [hiccup.table :refer [to-table1d]]))

;; --- UI --- ;;
(defn uuids-form 
  "Form composed of a textarea and a button.
   Expects UUIDs separated by newlines (as one would get from e.g.
   copying rows in a SQL client); 
   Sends UUIDs to /uuids, which grabs data from the database and
   creates a table to be shown in the UI in another element."
  [vs]
  [:form {:id "uuids-form" :name "uuids-form" :hx-post "/sendtolegal"
          :hx-target "this" :hx-swap "outerHTML"}
    (anti-forgery-field)
    [:label {:class "text-md text-gray-500 mb-2" :for "selected-uuids"} "Order UUIDs👇"]
    [:textarea {:id "selected-uuids" :name "selected-uuids" 
                :class "font-mono md:text-center text-xs md:text-sm text-gray-900 
                       w-full px-4 py-3 mt-2 rounded-md shadow bg-white 
                       focus:outline-none focus:ring focus:border-indigo-300"
                :rows 10 :cols 36 :required true :value (or vs "") 
                :hx-post "/uuids" 
                :hx-target "#report-preview" 
                :hx-trigger "keyup changed"
                :hx-indicator "#preview-indicator"}]
    [:div {:class "flex flex-row-reverse mt-1"}
      (ui/btn-disabled "uuid-form-submit-btn" "Publish to Slack")
      (ui/spinner "uuid-form-indicator" 8)]])

(def uuids-form-success
  "Replaces the uuids form when the backend process is successful."
   [:div {:id "uuids-success"} 
    [:div {:id "uuids-form-sucess-cell" 
           :class "px-4 py-3 md:mt-8 rounded-md shadow bg-white"}
      [:div {:class "flex justify-center"}
       [:svg {:xmlns "http://www.w3.org/2000/svg" 
              :viewBox "0 0 20 20" 
              :class "fill-current text-green-100 h-48"
              :fill "currentColor"}
         [:path {:fill-rule "evenodd" 
                 :d "M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 
                 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" 
                 :clip-rule "evenodd"}]]]]
    [:button {:hx-get "/uuids-form" 
              :hx-target "#uuids-success"
              :hx-swap "outerHTML"
              :class "float-right mt-3 items-center pl-4 text-sm 
                          font-medium text-indigo-500 focus:ring-2 
                          focus:ring-offset-2 focus:ring-indigo-500"}
     "Send another"]])

(def report-preview
  "Element to be replaced with the report preview."
  [:div {:id "report-preview" :name "report-preview"
         :class "overflow-auto py-3 px-4"}])

(defn preview-report
  "Shows a preview of the data that will be in the docx, and sent to Slack"
  []
  [:div {:class "mb-5 md:mb-0"}
    [:div {:class "flex flex-row"}
      [:h2 {:class "text-md text-gray-500 mb-2"} "Report preview"]
      (ui/spinner "preview-indicator" 6)]
    (ui/grid-cell "report-preview-cell" report-preview)])

(def table-attrs
  "Attributes for the table containing db data."
  {:table-attrs {:class "table-auto min-w-full divide-y divide-gray-200"}
   :thead-attrs {:class ""}
   :tbody-attrs {:class "bg-white divide-y divide-gray-200"}
   :data-tr-attrs {:class "px-6 py-4 text-sm"}
   :th-attrs {:class "px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"}
   :data-td-attrs {:class "px-6 py-4"}})

(defn report-fraud-page
  "Specific content from the fraud report page."
  []
  [:main
    [:section {:class "mt-5 mx-5 md:mx-20"}
     [:div {:class "grid grid-cols-1 md:grid-cols-4 gap-5"}
      [:div {:class "md:col-span-1"}
        (uuids-form "")]
      [:div {:class "md:col-span-3"}
        (preview-report)]]]])

;; --- SERVER --- ;;
(defroutes report-fraud-routes
  (GET "/publish-page" [_] (ui/base-page
                            (ui/common-header
                              (ui/nav-link "Publish" {} "underline")
                              (conj (ui/nav-link "Review")
                                    (ui/counter-label (:n-cases @db/db-data-holder)
                                                      "/n-review-cases")))
                            (report-fraud-page)))
  (POST "/uuids" req
        (info "Collecting data to show in UI")
        (let [raw-ids (get-in req [:params :selected-uuids])
              split-ids (string/split-lines raw-ids)
              coll (db/get-cases-by-uuid db/db {:ids split-ids})
              ks (keys (first coll))
              headers (->> (zipmap ks (mapv name ks)) 
                           (map (fn [[k v]] [k (jstrs/snakecase->normal v)]))
                           flatten
                           (into []))]
          (if (empty? coll)
            (html5 {}
                   report-preview
                   (ui/btn-disabled "uuid-form-submit-btn" 
                                    "Publish to Slack"
                                    {:hx-swap-oob "true"}))
            (do
             (info "Saving data in atom")
             (reset! db/db-data-holder coll)
             (info "Sending table to UI")
             (html5 
               {}
               (conj report-preview (to-table1d coll headers table-attrs)) 
               [:button {:type "submit" 
                         :id "uuid-form-submit-btn"
                         :hx-swap-oob "true"
                         :hx-swap "outerHTML"
                         :class "items-center px-4 py-2 border border-transparent 
                           rounded-md shadow text-sm font-medium text-white 
                           bg-indigo-600 hover:bg-indigo-700 focus:outline-none 
                           focus:ring-2 focus:ring-offset-2"}
                "Publish to Slack"])))))

  (GET "/uuids-form" [_]
       (html5 {}
              (uuids-form "")
              (ui/grid-cell-extra {:hx-swap-oob "#report-preview-cell"} 
                                  "report-preview-cell" 
                                  report-preview)))

  (POST "/sendtolegal" [_] 
    (let [rows @db/db-data-holder
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
      (html5 uuids-form-success)))) 
