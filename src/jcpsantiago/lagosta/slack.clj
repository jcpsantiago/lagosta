(ns jcpsantiago.lagosta.slack
  (:require [clj-slack.files :refer [upload]]
            [clojure.java.io :as io]))

(def slack-connection 
  {:api-url "https://slack.com/api"
   :token (System/getenv "LAGOSTA_SLACK_TOKEN")})

(defn upload-file!
  [slack-connection file-path]
  (upload slack-connection (io/input-stream file-path) 
          {:channels (System/getenv "LAGOSTA_CHANNEL_ID") 
           :title "This is a file" 
           :filename "output.docx" 
           :filetype "docx" 
           :initial_comment "👋 here's another file!"}))

(def upload-letter! (partial upload-file! slack-connection))
