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

(defquery get-coffee-by-id-query "sql/get-coffee-by-id.sql")
(defquery get-coffees-query "sql/get-coffees.sql")
(defquery get-coffees-by-roastery-query "sql/get-coffees-by-roastery.sql")
(defquery get-best-coffee-by-roastery-query "sql/get-best-coffee-by-roastery.sql")
(defquery get-user-by-name-query "sql/get-user-by-name.sql")
(defquery get-tastings-by-user-query "sql/get-tastings-by-user.sql")
(defquery get-tastings-by-coffee-query "sql/get-tastings-by-coffee.sql")
(defquery get-tasting-by-id-query "sql/get-tasting-by-id.sql")
(defquery get-roasteries-query "sql/get-roasteries.sql")
(defquery get-roastery-by-id-query "sql/get-roastery-by-id.sql")
(defquery create-tasting-query<! "sql/create-tasting.sql")
(defquery update-tasting-query! "sql/update-tasting.sql")
(defquery delete-tasting-query! "sql/delete-tasting.sql")
(defquery update-coffee-query! "sql/update-coffee.sql")

(defn get-coffee-by-id [id](first (get-coffee-by-id-query db-spec id)))

(defn get-coffees [] (get-coffees-query db-spec))

(defn get-coffees-by-roastery [roastery]
  (get-coffees-by-roastery-query db-spec (:roastery_id roastery)))

(defn get-best-coffee-by-roastery [roastery]
  (first (get-best-coffee-by-roastery-query db-spec (:roastery_id roastery))))

(defn get-roasteries []
  ;; XXX(miikka) Can this be done with one database query?
  (for [roastery (get-roasteries-query db-spec)
        :let [coffee (get-best-coffee-by-roastery roastery)]]
    (assoc roastery
      :coffee_id (:coffee_id coffee)
      :coffee_name (:coffee_name coffee))))

(defn get-roastery-by-id [id]
  (first (get-roastery-by-id-query db-spec id)))

(defn create-tasting [{:keys [type location rating notes coffee_id user_id]}]
  (create-tasting-query<! db-spec
                          type
                          location
                          rating
                          notes
                          coffee_id
                          user_id))

(defn update-tasting [id {:keys [type location rating notes coffee_id]}]
  (update-tasting-query! db-spec
                         type
                         location
                         rating
                         notes
                         coffee_id
                         id))

(defn delete-tasting! [id] (delete-tasting-query! db-spec id))

(defn ^:private user-roles [user]
  (if (:admin user) [:admin] []))

(defn get-user-by-name
  "Find an user by the the username. If the user does not exists, return nil."
  [username]
  (when-let [user (first (get-user-by-name-query db-spec username))]
    (assoc user :roles (user-roles user))))

(defn get-tastings-by-user [user]
  (get-tastings-by-user-query db-spec (:id user)))

(defn get-tastings-by-coffee [coffee]
  (get-tastings-by-coffee-query db-spec (:coffee_id coffee)))

(defn get-tasting-by-id [tasting-id]
  (first (get-tasting-by-id-query db-spec tasting-id)))

(defn update-coffee! [coffee-id {:keys [coffee_name roastery_id]}]
  (update-coffee-query! db-spec coffee_name roastery_id coffee-id))
