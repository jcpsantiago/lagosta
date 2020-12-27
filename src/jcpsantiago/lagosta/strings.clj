(ns jcpsantiago.lagosta.strings
  (:require [clojure.string :as string]))

(defn snakecase->normal
  "Converts a string s in snake_case to capitalized Normal text"
  [s]
  (-> (string/replace s "_" " ")
      string/capitalize))

