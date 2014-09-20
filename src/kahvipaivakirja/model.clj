(ns kahvipaivakirja.model
  (:require
   [yesql.core :refer [defquery]])
  (:import
   java.io.File))

(defn read-db-spec
  "Read the database configuration from ~/.kahvipaivakirja.edn. The
  format is the one used by clojure.java.jdbc."
  []
  (let [path (File. (System/getProperty "user.home") ".kahvipaivakirja.edn")]
    (assert (.canRead path))
    (read-string (slurp path))))

(def db-spec (read-db-spec))

(defquery get-coffees-query "sql/get-coffees.sql")
(defquery get-user-by-name-query "sql/get-user-by-name.sql")

(defn get-coffees [] (get-coffees-query db-spec))

(defn get-user-by-name
  "Find an user by the the username. If the user does not exists, return nil."
  [username]
  (first (get-user-by-name-query db-spec username)))
