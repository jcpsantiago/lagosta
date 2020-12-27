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

(def db
  {:dbtype "postgresql"
   :user "emmiakfyrpxhkf"
   :password "330f4f1893db17731bbbf4ddcbb71398ec84250d099353b64ebfd099941b6b9c"
   :host "ec2-54-235-181-55.compute-1.amazonaws.com"
   :port "5432"
   :dbname "d97ric957kug10"})

(def ds (jdbc/get-datasource db))
