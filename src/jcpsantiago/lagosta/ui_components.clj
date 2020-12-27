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
