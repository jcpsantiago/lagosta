(ns jcpsantiago.lagosta.ui-components
  "UI components used across modules")

(defn spinner
  "SVG spinner used as htmx-indicator"
  [id size]
  [:img {:id id 
         :src "/img/tail-spin.svg" 
         :class (str "w-" size " " 
                     "h-" size " " 
                     "mx-3 htmx-indicator")}])

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
