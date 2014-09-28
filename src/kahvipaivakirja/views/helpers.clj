(ns kahvipaivakirja.views.helpers
  (:require
   [clj-time.format :as f]
   [clj-time.coerce :refer [from-sql-time]]))

(def date-formatter (f/formatter "d.M.yyyy"))

(defn format-date
  [sql-time]
  (let [fmt (partial f/unparse date-formatter)]
    (-> sql-time from-sql-time fmt)))
