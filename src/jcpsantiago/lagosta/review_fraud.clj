(ns jcpsantiago.lagosta.review-fraud
  "This namespace has the UI and backend for the module
  to review fraud cases and assign a label to them."
  (:require [jcpsantiago.lagosta.ui-components :as ui]
            [jcpsantiago.lagosta.db :as db]
            [taoensso.timbre :refer [info]]
            [compojure.core :refer [defroutes GET POST]]
            [hiccup.page :refer [html5]]
            [org.httpkit.client :as http]
            [cheshire.core :as json]
            [clojure.string :refer [join]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]) 
             
  (:import java.net.URLEncoder))

(def gmaps-api-key (System/getenv "LAGOSTA_MAPS_EMBED_API_KEY"))

(defn static-map-widget
  "Static map image of the delivery location."
  [address]
  (let [urlencoded (URLEncoder/encode address)] 
    [:iframe {:class "w-full h-96"
              :src (str "https://www.google.com/maps/embed/v1/place?key="
                        gmaps-api-key
                        "&q=" urlencoded
                        "&zoom=17&maptype=satellite")}]))

(defn website-screenshot-widget
  "Shows a screenshot of the email's domain"
  [domain]
  [:a {:href "http://www.google.com" :target "_blank"}
   [:img {:class "mt-4" 
          ; FIXME the target should be more dynamic i.e. an env
          :src (str "http://localhost:2341/?url=" domain)}]])

(defn uuid-input
  "Form to input uuid for getting db data"
  []
  [:div {:class "mx-20 flex mb-4"}
    [:form {:id "uuids-form" :name "uuids-form"} 
      (anti-forgery-field)
      [:label {:class "block text-md text-gray-500 mb-2" :for "selected-uuids"} "Your UUID👇"]
      [:div {:class "flex items-center"}
        [:input {:id "uuid" :name "uuid" 
                 :type "text"
                 :class "font-mono md:text-center text-xs md:text-sm text-gray-900 
                       w-96 px-4 py-3 mt-2 rounded-md shadow bg-white 
                       focus:outline-none focus:ring focus:border-indigo-300"
                 :hx-post "/review-page" 
                 :hx-target "#case-info" 
                 :hx-trigger "keyup changed"
                 :hx-indicator "#uuid-form-indicator"}]
        (ui/spinner "uuid-form-indicator" 8)]]])

(defn review-page
  "Specific elements to the review fraud page."
  [uuid]
  (let [raw-data (db/get-review-by-uuid db/ds {:uuid uuid})
        company-name (:company_name raw-data)
        domain (->> (:email raw-data)
                    (re-matches #".+@(.+)")
                    second
                    (str "http://"))
        d-address (:delivery_address raw-data)]
    [:div {:id "case-info" 
           :class "mx-20"}
      (ui/grid-cell "review-page-content"
        [:div {:class "flex"}
         [:div {:class "w-96"}
           (static-map-widget d-address)
           (website-screenshot-widget domain)]
         [:div {:class "ml-8"}
          [:h2 {:class "font-bold text-2xl"} company-name]
          [:div {:class "grid grid-cols-6"}]]])]))

(defroutes review-fraud-routes
  (GET "/review-page" [_] (ui/base-page 
                            (ui/common-header
                              (ui/nav-link "Publish")
                              (conj (ui/nav-link "Review" {} "underline")
                                    (ui/counter-label "/n-review-cases")))
                            (uuid-input)
                            [:div {:id "case-info"}]))

  (POST "/review-page" req
        (let [uuid (get-in req [:params :uuid])]
          (info "setting review page")
          (html5 
            (review-page uuid)))))
