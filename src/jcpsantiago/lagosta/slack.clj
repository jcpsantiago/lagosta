(ns jcpsantiago.lagosta.slack
  (:require [clj-slack.files :refer [upload]]
            [clojure.java.io :as io]))

(def slack-connection 
  {:api-url "https://slack.com/api"
   :token (System/getenv "LAGOSTA_SLACK_TOKEN")})

(defn upload-file!
  [slack-connection file-path]
  (upload slack-connection (io/input-stream file-path) 
          {:channels (System/getenv "LAGOSTA_SLACK_CHANNEL")
           :title "Report for the police" 
           :filename "fraud_case_letter.docx" 
           :filetype "docx" 
           :initial_comment "Here's another fraud case to share with the authorities."}))

(def upload-letter! (partial upload-file! slack-connection))
