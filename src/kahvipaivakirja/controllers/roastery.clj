(ns kahvipaivakirja.controllers.roastery
  "Controllers for roastery CRUD operations."
  (:refer-clojure :exclude [list])
  (:use
   kahvipaivakirja.model
   kahvipaivakirja.util)
  (:require
   [formative.parse :refer [parse-params]]
   [kahvipaivakirja.forms :as forms]
   [kahvipaivakirja.views :as views]))

(defn ^:private get-roastery [req]
  (when-let [id (get-id req)] (get-roastery-by-id id)))

(defn ^:private roastery-url [roastery]
  (let [roastery_id (or (:roastery_id roastery) (:id roastery))]
    (format "/roastery/%d/" roastery_id)))

(defn ^:private parse-roastery [req]
  (parse-params (forms/roastery-form) (:params req)))

(defn list [req]
  (render req views/roastery-ranking-page (get-roasteries)))

(defn display [req]
  (when-let [roastery (get-roastery req)]
    (let [coffees (get-coffees-by-roastery roastery)]
      (render req views/roastery-info-page roastery coffees))))

(defn edit [req]
  (when-let [roastery (get-roastery req)]
    (render req views/edit-roastery-page roastery {})))

(defn save-edit [req]
  (when-let [roastery (get-roastery req)]
    (try
      (let [params (parse-roastery)]
        (update-roastery! (:roastery_id roastery) params)
        (redirect req (roastery-url roastery)))
      (catch clojure.lang.ExceptionInfo ex
        (let [problems (:problems (ex-data ex))]
          (render req views/edit-roastery-page (:params req) problems))))))

(defn create [req]
  (render req views/add-roastery-page {} {}))

(defn save-new [req]
  (try
    (let [params (parse-roastery)]
      (let [roastery (create-roastery<! params)]
        (redirect req (roastery-url roastery))))
    (catch clojure.lang.ExceptionInfo ex
      (let [problems (:problems (ex-data ex))]
        (render req views/add-roastery-page (:params req) problems)))))

(defn delete [req]
  (when-let [roastery (get-roastery req)]
    (delete-roastery! roastery)
    (redirect req "/roastery/")))
