(ns kahvipaivakirja.controllers.coffee
  "Controllers for coffee CRUD operations."
  (:refer-clojure :exclude [list])
  (:use
   kahvipaivakirja.model
   kahvipaivakirja.util)
  (:require
   [formative.parse :refer [parse-params]]
   [kahvipaivakirja.forms :as forms]
   [kahvipaivakirja.views :as views]))

(defn ^:private get-coffee [req]
  (when-let [id (get-id req)] (get-coffee-by-id id)))

(defn ^:private coffee-url [coffee]
  (let [coffee_id (or (:coffee_id coffee) (:id coffee))]
    (format "/coffee/%d/" coffee_id)))

(defn list [req]
  (render req views/coffee-ranking-page (get-coffees)))

(defn display [req]
  (when-let [coffee (get-coffee req)]
    (let [tastings (get-tastings-by-coffee coffee)]
      (render req views/coffee-info-page coffee tastings))))

(defn edit [req]
  (when-let [coffee (get-coffee req)]
    (let [roasteries (get-roasteries)]
      (render req views/edit-coffee-page coffee roasteries {}))))

(defn save-edit [req]
  (when-let [coffee (get-coffee req)]
    (let [roasteries (get-roasteries)]
      (try
        (let [params (parse-params (forms/coffee-form roasteries) (:params req))]
          (update-coffee! (:coffee_id coffee) params)
          (redirect req (coffee-url coffee)))
        (catch clojure.lang.ExceptionInfo ex
          (let [problems (:problems (ex-data ex))]
            (render req views/edit-coffee-page (:params req) roasteries problems)))))))

(defn create [req]
  (render req views/add-coffee-page (:params req) (get-roasteries) {}))

(defn save-new [req]
  (let [roasteries (get-roasteries)]
    (try
      (let [params (parse-params (forms/coffee-form roasteries) (:params req))]
        (let [coffee (create-coffee<! params)]
          (redirect req (coffee-url coffee))))
      (catch clojure.lang.ExceptionInfo ex
        (let [problems (:problems (ex-data ex))]
          (render req views/add-coffee-page (:params req) roasteries problems))))))

(defn delete [req]
  (when-let [coffee (get-coffee req)]
    (delete-coffee! (:coffee_id coffee))
    (redirect req "/coffee/")))
