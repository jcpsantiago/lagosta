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
                     "mx-2 htmx-indicator")}])

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
  ([txt endpoint]
   (counter-label txt endpoint {}))
  ([txt endpoint extra-attrs]
   (let [base-attrs {:id (str (string/replace endpoint #"/" "") "-counter")
                     :class "ml-1 text-gray-400 text-sm font-medium" 
                     :hx-get endpoint
                     :hx-swap "outerHTML"
                     :hx-trigger "load delay:14400s"}]
     [:span (conj base-attrs extra-attrs) txt])))

(defn common-header
  "Creates the header seen in every page"
  [& elements]
  [:header {:class "my-5"}
   [:nav {:hx-boost "true"}
    [:div {:class "flex-1 flex items-center justify-left"}
     [:h1 {:class "font-mono 2xl text-indigo-500 mr-8"} 
      [:a {:href "/"} "Lagosta🦞"]]
     elements]]])

(defn base-page 
  "Skeleton used for every page"
  [header & elements]
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
     [:div {:class "flex flex-col h-screen justify-between
                   mx-5 md:mx-20"}
      header
      [:main {:class "mb-auto"}
       [:section
         elements]]
      [:footer {:class "mt-5"}
       ; FIXME should be global var
        [:p {:class "text-gray-300 text-xs"} "0.1.0-SNAPSHOT"]]]]))

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

(def refresh-sm
  "Small refresh icon from heroicons.com"
  [:svg {:xmlns "http://www.w3.org/2000/svg"
         :viewbox "0 0 20 20"
         :fill "currentColor"} 
    [:path {:fill-rule "evenodd"
            :d "M4 2a1 1 0 011 1v2.101a7.002 7.002 0 0111.601 2.566 1 1 0 11-1.885.666A5.002 5.002 0 005.999 7H9a1 
               1 0 010 2H4a1 1 0 01-1-1V3a1 1 0 011-1zm.008 9.057a1 1 0 011.276.61A5.002 5.002 0 0014.001 13H11a1 
               1 0 110-2h5a1 1 0 011 1v5a1 1 0 11-2 0v-2.101a7.002 7.002 0 01-11.601-2.566 1 1 0 01.61-1.276z"
            :clip-rule "evenodd"}]])
