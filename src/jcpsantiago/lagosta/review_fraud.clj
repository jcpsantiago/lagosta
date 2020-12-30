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
              :allowfullscreen true
              :src (str "https://www.google.com/maps/embed/v1/place?key="
                        gmaps-api-key
                        "&q=" urlencoded
                        "&zoom=17&maptype=satellite")}]))

(defn website-screenshot-widget
  "Shows a screenshot of the email's domain"
  [url]
  [:a {:href url :target "_blank"}
   [:img {:class "mt-4" 
          ; FIXME the target should be more dynamic i.e. an env
          :src (str "http://localhost:2341/?url=" url)}]])

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

(defn review-placeholder-page
  "Creates a container with placeholders for the fraud review page"
  []
  [:div {:class "mx-20"}
    (ui/grid-cell "case-info-container"
      [:div {:id "case-info" 
             :hx-swap "outerHTML"
             :class "flex"}
       [:div {:class "w-96"}
        [:div {:class "flex items-center w-full h-96 rounded-md border-4 border-dashed border-gray-200 border-opacity-100"}
         [:div {:class "mx-auto w-12 text-gray-200"}
          [:svg {:xmlns "http://www.w3.org/2000/svg"
                 :fill "none"
                 :viewbox "0 0 24 24"
                 :stroke "currentColor"}
            [:path {:stroke-linecap "round"
                    :stroke-linejoin "round"
                    :stroke-width "2"
                    :d "M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 
                       011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 
                       1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7"}]]]]
        [:div {:class "mt-4 flex items-center w-full h-80 rounded-md border-4 border-dashed border-gray-200 border-opacity-100"}
         [:div {:class "mx-auto w-12 text-gray-200"}
           [:svg {:xmlns "http://www.w3.org/2000/svg"
                  :fill "none"
                  :viewbox "0 0 24 24"
                  :stroke "currentColor"}
             [:path {:stroke-linecap "round"
                     :stroke-linejoin "round"
                     :stroke-width "2"
                     :d "M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 
                       9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 
                       3-9m-9 9a9 9 0 019-9"}]]]]]
       [:div {:class "ml-4 flex-1"}
        [:div {:class "flex items-center h-full h-96 rounded-md border-4 border-dashed border-gray-200 border-opacity-100"}
         [:div {:class "mx-auto w-12 text-gray-200"}
           [:svg {:xmlns "http://www.w3.org/2000/svg"
                  :fill "none"
                  :viewbox "0 0 24 24"
                  :stroke "currentColor"} 
             [:path {:stroke-linecap "round"
                     :stroke-linejoin "round"
                     :stroke-width "2"
                     :d "M3 10h18M3 14h18m-9-4v8m-7 0h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 
                       2 0 00-2 2v8a2 2 0 002 2z"}]]]]]])])
  
(defn case-info
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
           :hx-swap "outerHTML"
           :class "flex"}
     [:div {:class "w-96"}
       (static-map-widget d-address)
       (website-screenshot-widget domain)]
     [:div {:class "ml-8"}
      [:h2 {:class "font-bold text-2xl"} company-name]
      [:div {:class "grid grid-cols-6"}]]]))

(defroutes review-fraud-routes
  (GET "/review-page" [_] (ui/base-page 
                            (ui/common-header
                              (ui/nav-link "Publish")
                              (conj (ui/nav-link "Review" {} "underline")
                                    (ui/counter-label "/n-review-cases")))
                            (uuid-input)
                            (review-placeholder-page)))

  (POST "/review-page" req
        (let [uuid (get-in req [:params :uuid])]
          (info "setting review page")
          (html5 
            (case-info uuid)))))
