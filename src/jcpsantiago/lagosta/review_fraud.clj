(ns jcpsantiago.lagosta.review-fraud
  "This namespace has the UI and backend for the module
  to review fraud cases and assign a label to them."
  (:require [jcpsantiago.lagosta.ui-components :as ui]
            [jcpsantiago.lagosta.db :as db]
            [jcpsantiago.lagosta.strings :as jstrs]
            [taoensso.timbre :refer [info warn]]
            [compojure.core :refer [defroutes GET POST]]
            [hiccup.page :refer [html5]]
            [clojure.string :refer [join trim]]
            [ring.util.anti-forgery :refer [anti-forgery-field]] 
            [hiccup.table :refer [to-table1d]])
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
                        "&zoom=19&maptype=satellite")}]))

(defn website-screenshot-widget
  "Shows a screenshot of the email's domain"
  [url]
  [:a {:href url :target "_blank"}
   [:img {:class "h-60 w-full" 
          ; FIXME the target should be more dynamic i.e. an env
          :src (str "http://localhost:2341/?url=" url)}]])

(defn uuid-input
  "Just the input field of the uuid form"
  [txt]
  [:input (conj 
            {:id "uuid-input" :name "uuid" :value txt
             :type "text" :placeholder "ab29fd12-d628-4fc7-9558-750b77d525a5"
             :class "font-mono md:text-center text-xs md:text-sm text-gray-800 
                     w-96 px-4 py-3 rounded-md shadow bg-white placeholder-gray-200 
                     focus:outline-none focus:ring focus:border-indigo-300"
             :hx-get "/case-info" 
             :hx-target "#case-info" 
             :hx-indicator "#uuid-form-indicator"}
            (if (= txt "") {} {:hx-swap-oob "true"}))])

(defn uuid-form
  "Pseudo-form to input uuid for getting db data"
  []
  [:div {:class "mb-4"}
    [:label {:class "block text-md text-gray-600" :for "selected-uuids"} "Order UUID👇"]
    [:div {:class "flex items-center mt-2"}
      (uuid-input "")
      [:div {:class "ml-2 w-6 text-gray-600 hover:text-indigo-500 cursor-pointer" 
             :hx-get "/case-info"
             :hx-trigger "click"
             :hx-indicator "#uuid-form-indicator"
             :hx-target "#case-info"}
       ui/refresh-sm]
      (ui/spinner "uuid-form-indicator" 7)]])
      ; [:div {:class ""}
      ;  [:div {:class ""}
      ;   [:a {:href "#"} "👎"]]
      ;  [:div {:class ""}
      ;   [:a {:href "#"} "🤷"]]
      ;  [:div {:class ""}
      ;   [:a {:href "#"} "👍"]]]]])


(defn review-placeholder-page
  "Creates a container with placeholders for the fraud review page"
  []
  [:div {:id "case-info" 
         :class "flex"}
   [:div {:class "w-96"}
    [:div {:class "mb-4 flex items-center w-full h-60 rounded-md border-4 border-dashed border-gray-200 border-opacity-100"}
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
                       3-9m-9 9a9 9 0 019-9"}]]]]
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
                       1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7"}]]]]]
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
                       2 0 00-2 2v8a2 2 0 002 2z"}]]]]]])

(defn emptystr->nil
  "If s is an empty string return nil, otherwise return s"
  [s]
  (if (= s "") nil s))

(def table-attrs
  "Attributes for the table containing db data."
  {:table-attrs {:class "table-auto min-w-full"}
   :thead-attrs {:class ""}
   :tbody-attrs {:class "bg-white"}
   :data-tr-attrs {:class "pb-2"}
   :th-attrs {:class "text-left text-md text-indigo-500 font-medium pr-4"}
   :data-td-attrs {:class "text-md"}
   :data-value-transform (fn [k v]
                            (if (= :order_items k) ; name set in cases.sql!
                              (jstrs/truncate v 55)
                              v))})
  
(defn case-info
  "Specific elements to the review fraud page."
  ([uuid]
   (if (some #{uuid} (:uuids @db/db-data-holder))
     (let [case-data (->> (filter #(= (:uuid %) uuid) (:cases @db/db-data-holder))
                          (into {}))
           order-items (as-> (:order-items @db/db-data-holder) $
                         (filter #(= (:uuid %) uuid) $)
                         (into {} $)
                         (dissoc $ :uuid))]
      (case-info case-data order-items))
    (do
      (info "Getting data for UUID not present in review case list")
      (let [case-data (future (db/get-review-by-uuid db/ds {:uuid uuid}))
            order-items (future (->> (db/get-orderitems-by-uuid db/ds {:uuid uuid})
                                     (mapv #(dissoc % :uuid))))]
        (case-info @case-data @order-items)))))
  ([case-data order-items]
   (let [ks (keys (first order-items))
         headers (->> (zipmap ks (mapv name ks)) 
                      (map (fn [[k v]] [k (jstrs/snakecase->normal v)]))
                      flatten
                      (into []))
         review-type (:review_type case-data)
         merchant (:merchant case-data)
         first-name (:first_name case-data)
         last-name (:last_name case-data)
         full-name (-> (join " " [first-name last-name])
                       trim
                       emptystr->nil)
         created-at (:created_at case-data)
         phone (:phone case-data)
         company-name (:company_name case-data)
         schufa-id (:schufa_id case-data)
         company-type (if (> (count schufa-id) 10)
                        "sole-trader"
                        "company")
         order-amount (:amount_gross case-data)
         email (:email case-data)
         ea-score (:emailage_score case-data)
         domain (->> email 
                     (re-matches #".+@(.+)")
                     second
                     (str "http://"))
         b-address (:billing_address case-data)
         d-address (:delivery_address case-data)]
     [:div {:id "case-info" 
            :class "flex"}
      [:div {:class "w-96"}
        [:div {:class "mb-4"} 
         (website-screenshot-widget domain)]
        (static-map-widget d-address)]
      [:div {:class "ml-16"}
       [:div {:class "inline-block text-gray-400 text-xs mb-1"} review-type]
       [:div {:class "mb-16"}
         [:h2 {:class "font-bold text-2xl text-gray-800"} company-name]
         [:div {:class "font-medium text-gray-400"} 
          (str company-type " ordering at " merchant " on " created-at)]]
       [:div {:class "grid grid-cols-2 gap-4"}
         [:div
          [:h3 {:class "text-md text-indigo-500 font-medium"} "Recipient's full name"]
          (if (nil? full-name)
            [:p {:class "text-md text-gray-200"} "Not available"]
            [:p {:class "text-md"} full-name])]
         [:div
          [:h3 {:class "text-md text-indigo-500 font-medium"} "Phone number"]
          (if (nil? phone)
            [:p {:class "text-md text-gray-200"} "Not available"]
            [:p {:class "text-md"} phone])]
         [:div
          [:h3 {:class "text-md text-indigo-500 font-medium"} "Email"]
          [:p {:class "text-md"} email]]
         [:div 
          [:h3 {:class "text-md text-indigo-500 font-medium"} 
           "Emailge score"
           [:a {:href "https://portal.emailage.com/#/emailrisk-score/quick-query" 
                :target "_blank"}
             ui/ext-link-sm]]
          (if (nil? ea-score)
            [:p {:class "text-md text-gray-200"} "Not available"]
            [:p {:class "text-md"} ea-score])]
         [:div
          [:h3 {:class "text-md text-indigo-500 font-medium"} "Delivery address"
           [:a {:href (str "http://www.google.com/search?q=" (URLEncoder/encode d-address)) 
                :target "_blank"}
             ui/ext-link-sm]]
          [:p {:class "text-md"} d-address]]
         [:div
          [:h3 {:class "text-md text-indigo-500 font-medium"} "Billing address"]
          [:p {:class "text-md"} b-address]]
         [:div
          [:h3 {:class "text-md text-indigo-500 font-medium"} "Total order amount"]
          [:p {:class "text-md"} order-amount]]
         [:div {:class "col-span-2"}
          (if (every? nil? (vals (first order-items)))
            [:h3 {:class "text-md text-indigo-500 font-medium"} "Order items"
              [:p {:class "text-md text-gray-200"} "Not available"]]
            (to-table1d order-items headers table-attrs))]]]])))
  

(defroutes review-fraud-routes
  (GET "/review-page" [_] 
       (ui/base-page 
         (ui/common-header
           (ui/nav-link "Publish")
           (conj (ui/nav-link "Review" {} "underline")
                 (ui/counter-label (:n-cases @db/db-data-holder) 
                                   "/n-review-cases")))
         (uuid-form)
         [:div {:class ""}
           (ui/grid-cell "case-info-container" (review-placeholder-page))]))

  (GET "/case-info" req 
       (let [uuid (-> (get-in req [:params :uuid])
                      jstrs/find-uuid)]
         (cond 
           (seq uuid) (html5 (case-info uuid))
           (nil? uuid) (let [uuid (rand-nth (:uuids @db/db-data-holder))]
                         (info "setting review page")
                         (html5 
                           (case-info uuid)
                           (uuid-input uuid)))
           (= uuid "")  (html5 (review-placeholder-page))
           :else (warn "Invalid UUID"))))) 
