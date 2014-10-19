(ns kahvipaivakirja.model
  "Database interface for Kahvipäiväkirja.

  We use yesql, so the actual SQL code is in .sql files under
  src/sql/. The database settings are stored in the file
  ~/.kahvipaivakirja.edn and read in to the variable db-spec.

  The naming convention:
  * SELECT: get-foo-by-bar, get-foos
  * UPDATE/DELETE: update-foo!, delete-foo!
  * INSERT: create-foo<!

  create-foo<! means that it returns the row it created."
  (:require
   [clojure.java.jdbc :as jdbc]
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

(declare update-tasting-coffee-query!)

;;; COFFEE QUERIES

(defquery get-coffee-by-id-query "sql/get-coffee-by-id.sql")
(defquery get-coffees-query "sql/get-coffees.sql")
(defquery get-coffees-by-roastery-query "sql/get-coffees-by-roastery.sql")
(defquery get-best-coffee-by-roastery-query "sql/get-best-coffee-by-roastery.sql")
(defquery create-coffee-query<! "sql/create-coffee.sql")
(defquery update-coffee-query! "sql/update-coffee.sql")
(defquery update-coffee-roastery-query! "sql/update-coffee-roastery.sql")
(defquery delete-coffee-query! "sql/delete-coffee.sql")

(defn get-coffee-by-id [id] (first (get-coffee-by-id-query db-spec id)))
(defn get-coffees [] (get-coffees-query db-spec))

(defn get-coffees-by-roastery [roastery]
  (get-coffees-by-roastery-query db-spec (:roastery_id roastery)))

(defn get-best-coffee-by-roastery [roastery]
  (first (get-best-coffee-by-roastery-query db-spec (:roastery_id roastery))))

(defn create-coffee<! [{:keys [coffee_name roastery_id]}]
  (create-coffee-query<! db-spec
                         coffee_name
                         roastery_id))

(defn update-coffee! [coffee-id {:keys [coffee_name roastery_id]}]
  (update-coffee-query! db-spec coffee_name roastery_id coffee-id))

(defn delete-coffee! [coffee-id]
  (delete-coffee-query! db-spec coffee-id))

(defn merge-coffees! [target-id source-id]
  (jdbc/with-db-transaction [connection db-spec]
    (update-tasting-coffee-query! connection target-id source-id)
    (delete-coffee-query! connection source-id)))

;;; ROASTERY QUERIES

(defquery get-roasteries-query "sql/get-roasteries.sql")
(defquery get-roastery-by-id-query "sql/get-roastery-by-id.sql")
(defquery create-roastery-query<! "sql/create-roastery.sql")
(defquery update-roastery-query! "sql/update-roastery.sql")
(defquery delete-roastery-query! "sql/delete-roastery.sql")

(defn get-roasteries []
  ;; XXX(miikka) Can this be done with one database query?
  (for [roastery (get-roasteries-query db-spec)
        :let [coffee (get-best-coffee-by-roastery roastery)]]
    (assoc roastery
      :coffee_id (:coffee_id coffee)
      :coffee_name (:coffee_name coffee))))

(defn get-roastery-by-id [id]
  (first (get-roastery-by-id-query db-spec id)))

(defn create-roastery<! [{:keys [roastery_name]}]
  (create-roastery-query<! db-spec roastery_name))

(defn update-roastery! [roastery-id {:keys [roastery_name]}]
  (update-roastery-query! db-spec roastery_name roastery-id))

(defn delete-roastery! [roastery-id]
  (delete-roastery-query! db-spec roastery-id))

(defn merge-roasteries! [target-id source-id]
  (jdbc/with-db-transaction [connection db-spec]
    (update-coffee-roastery-query! connection target-id source-id)
    (delete-roastery-query! connection source-id)))

;;; TASTING QUERIES

(defquery get-tastings-by-user-query "sql/get-tastings-by-user.sql")
(defquery get-tastings-by-coffee-query "sql/get-tastings-by-coffee.sql")
(defquery get-tasting-by-id-query "sql/get-tasting-by-id.sql")
(defquery create-tasting-query<! "sql/create-tasting.sql")
(defquery update-tasting-query! "sql/update-tasting.sql")
(defquery update-tasting-coffee-query! "sql/update-tasting-coffee.sql")
(defquery delete-tasting-query! "sql/delete-tasting.sql")

(defn get-tastings-by-user [user]
  (get-tastings-by-user-query db-spec (:id user)))

(defn get-tastings-by-coffee [coffee]
  (get-tastings-by-coffee-query db-spec (:coffee_id coffee)))

(defn get-tasting-by-id [tasting-id]
  (first (get-tasting-by-id-query db-spec tasting-id)))

(defn create-tasting<! [{:keys [type location rating notes coffee_id user_id]}]
  (create-tasting-query<! db-spec
                          type
                          location
                          rating
                          notes
                          coffee_id
                          user_id))

(defn update-tasting! [id {:keys [type location rating notes coffee_id]}]
  (update-tasting-query! db-spec
                         type
                         location
                         rating
                         notes
                         coffee_id
                         id))

(defn delete-tasting! [id] (delete-tasting-query! db-spec id))

;; USER QUERIES

(defquery get-user-by-name-query "sql/get-user-by-name.sql")

(defn ^:private user-roles [user]
  (if (:admin user) [:admin] []))

(defn get-user-by-name
  "Find an user by the the username. If the user does not exists, return nil."
  [username]
  (when-let [user (first (get-user-by-name-query db-spec username))]
    (assoc user :roles (user-roles user))))
