(ns jcpsantiago.lagosta.db
  (:require [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as adapter]
            [next.jdbc :as jdbc]))

(def db-data-holder
  "Holds data collected from the db"
  (atom {}))

;; regular SQL functions
(hugsql/def-db-fns "sql/cases.sql"
                   {:adapter (adapter/hugsql-adapter-next-jdbc)})

; FIXME write function that outputs this map based on the type
; of db we want. Snowflake is needed for my testing
(def db
  {:dbtype "snowflake"
   :user (System/getenv "LAGOSTA_DB_USER")
   :password (System/getenv "LAGOSTA_DB_PASSWORD")
   :host (System/getenv "LAGOSTA_DB_HOST")
   :port "443"
   :db (System/getenv "LAGOSTA_DB_DB")   
   :roles (System/getenv "LAGOSTA_DB_ROLE")
   :classname "net.snowflake.client.jdbc.SnowflakeDriver"})

(def ds (jdbc/get-datasource db))
