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

(defquery get-coffees "sql/get-coffees.sql")
