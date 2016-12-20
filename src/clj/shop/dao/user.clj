(ns shop.dao.user
  (:require [shop.db.core :refer [db]]
            [clojure.java.jdbc :as j]))

(defn getUserByName [username] 
  (first (j/query db
    ["select * from users where username = ?" username])))

(defn insertNew [creds]
  (j/insert! db :users {:username (:username creds) :password (:password creds)})
  (getUserByName (:username creds)))
