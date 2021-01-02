(ns jcpsantiago.lagosta.strings
  (:require [clojure.string :as string]))

(defn find-uuid
  "If s is an UUID, return it, otherwise return nil.
  If s is nil return nil."
  [s]
  (try (re-matches #"^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$" s)
       (catch Exception _ nil)))

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
