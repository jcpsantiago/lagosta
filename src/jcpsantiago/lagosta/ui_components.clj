(ns jcpsantiago.lagosta.ui-components
  "UI components used across modules"
  (:require [hiccup.page :refer [html5 include-css include-js]]))

(defn spinner
  "SVG spinner used as htmx-indicator"
  [id size]
  [:img {:id id 
         :src "/img/tail-spin.svg" 
         :class (str "w-" size " " 
                     "h-" size " " 
                     "mx-3 htmx-indicator")}])

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
         [:a {:href "/publish-page"
              :class nav-link-class}
          "Publish"]]
       [:div {:class "mr-4"}
         [:a {:href "/review-page"
              :class nav-link-class}
          "Review"]
         [:span {:class "ml-1 text-gray-400 text-sm font-medium" 
                 :hx-get "/n-review-cases"
                 :hx-swap "outerHTML"
                 :hx-trigger "load"}
          ""]]]]]))

(defn base-page [& elements]
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
     elements]))

(defn grid-cell-extra 
  "Grid element to contain other elements"
  [extra-attrs id & elements]
  (let [base-attrs {:id id 
                    :class "px-4 py-3 rounded-md shadow bg-white"}]
    [:div (conj base-attrs extra-attrs) elements]))

(def grid-cell (partial grid-cell-extra {}))

(defn btn-disabled
  "Disabled, greyed-out button with default non-interactive cursor"
  ([id txt]
   (btn-disabled id txt {}))
  ([id txt extra-attrs]
   (let [base-attrs {:type "submit" 
                     :id id
                     :disabled true
                     :class "items-center px-4 py-2 border-2 
                             rounded-md text-sm font-medium text-gray-300 
                             border-gray-300 focus:outline-none cursor-default
                             focus:ring-2 focus:ring-offset-2"}]
     [:button (conj base-attrs extra-attrs) txt])))
