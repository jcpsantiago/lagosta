(ns jcpsantiago.lagosta.ui-components
  "UI components used across modules"
  (:require [hiccup.page :refer [html5 include-css include-js]]
            [clojure.string :as string]))

(defn spinner
  "SVG spinner used as htmx-indicator"
  [id size]
  [:img {:id id 
         :src "/img/tail-spin.svg" 
         :class (str "w-" size " " 
                     "h-" size " " 
                     "mx-3 htmx-indicator")}])

(def classes "underline")

(defn nav-link
  "Creates a navigation link shown in the header of every page"
  ([txt]
   (nav-link txt {}))
  ([txt extra-attrs & extra-classes]
   (let [safe-txt (-> (string/lower-case txt)
                      (string/replace #" " "-"))
         base-classes "text-sm text-gray-800 font-semibold hover:text-indigo-500"
         all-classes (conj extra-classes base-classes)]
     [:div (conj {:id (str safe-txt "-nav-link") 
                  :class "mr-4"} 
                 extra-attrs)
       [:a {:href (str "/" safe-txt "-page")
            :class (string/join " " all-classes)}
        txt]])))

(defn counter-label
  "Element showing a number. Used e.g. next to nav links"
  ([endpoint]
   (counter-label "" endpoint {}))
  ([txt endpoint extra-attrs]
   (let [base-attrs {:id (str (string/replace endpoint #"/" "") "-counter")
                     :class "ml-1 text-gray-400 text-sm font-medium" 
                     :hx-get endpoint
                     :hx-swap "outerHTML"
                     :hx-trigger "load"}]
     [:span (conj base-attrs extra-attrs) txt])))

(defn common-header
  "Creates the hiccup to construct the header seen in every page"
  [& elements]
  [:header {:class "mt-5 mx-5 md:mx-20 mb-10 md:mb-12"}
   [:nav {:hx-boost "true"}
    [:div {:class "flex-1 flex items-center justify-left"}
     [:h1 {:class "font-mono 2xl text-indigo-500 mr-8"} 
      [:a {:href "/"} "Lagosta🦞"]]
     elements]]])

(defn base-page [& elements]
  (html5
    {:class "" :lang "en"}
    [:head
     (include-css "https://unpkg.com/tailwindcss@2.0.2/dist/tailwind.min.css")
     (include-js "https://unpkg.com/htmx.org@1.0.2")
     [:link {:rel "apple-touch-icon" 
             :sizes "180x180" 
             :href "/img/apple-touch-icon.png"}]
     [:link {:rel "icon" 
             :type "image/png"
             :sizes "32x32"  
             :href "/img/favicon-32x32.png"}]
     [:link {:rel "icon" 
             :type "image/png"
             :sizes "16x16"  
             :href "/img/favicon-16x16.png"}]
     [:link {:rel "manifest" 
             :href "/site.webmanifest"}]
     [:title "Lagosta"]
     [:meta
      {:charset "utf-8",
       :name "viewport",
       :content "width=device-width, initial-scale=1.0"}]]
    [:body {:class "bg-red-50"}
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


;; --- SVG ICONS --- ;;
(def ext-link-sm
  "Small external link icon from heroicons.com"
  [:svg {:class "inline h-5" :xmlns "http://www.w3.org/2000/svg", :viewbox "0 0 20 20", :fill "currentColor"}
   [:path {:d "M11 3a1 1 0 100 2h2.586l-6.293 6.293a1 1 0 101.414 1.414L15 6.414V9a1 1 0 102 0V4a1 1 0 00-1-1h-5z"}]
   [:path {:d "M5 5a2 2 0 00-2 2v8a2 2 0 002 2h8a2 2 0 002-2v-3a1 1 0 10-2 0v3H5V7h3a1 1 0 000-2H5z"}]])
