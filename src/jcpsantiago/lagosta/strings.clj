(ns jcpsantiago.lagosta.strings
  (:require [clojure.string :as string]))

(defn snakecase->normal
  "Converts a string s in snake_case to capitalized Normal text"
  [s]
  (-> (string/replace s "_" " ")
      string/capitalize))

(defn truncate
  "Truncate a string with suffix (ellipsis by default) if it is
   longer than specified length. From open-company/clojure-humanize"
  ([string length suffix]
   (let [string-len (count string)
         suffix-len (count suffix)]
     (if (<= string-len length)
       string
       (str (subs string 0 (- length suffix-len)) suffix))))
  ([string length]
   (truncate string length "...")))
