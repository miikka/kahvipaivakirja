(ns kahvipaivakirja.controllers.tasting
  "Controllers for tasting CRUD operations."
  (:use
   kahvipaivakirja.model
   kahvipaivakirja.util)
  (:require
   [formative.parse :refer [parse-params]]
   [kahvipaivakirja.forms :as forms]
   [kahvipaivakirja.views :as views]))

(defn ^:private current-user-id [req]
  (:id (current-user req)))

(defn ^:private get-tasting [req]
  (when-let [id (get-id req)] (get-tasting-by-id id)))

(defn ^:private get-own-tasting [req]
  "Returns the tasting by request :id parameter, but only when it's
  owned by the current user. Otherwise returns nil."
  (when-let [tasting (get-tasting req)]
    (when (= (current-user-id req) (:user_id tasting))
      tasting)))

(defn edit [req]
  (when-let [tasting (get-own-tasting req)]
    (render req views/edit-tasting-page tasting (get-coffees) [])))

(defn save-edit [req]
  (when-let [tasting (get-own-tasting req)]
    (let [coffees (get-coffees)]
      (try
        (let [params (parse-params (forms/tasting-form coffees) (:params req))]
          (update-tasting! (:id tasting) params)
          (redirect req "/tasting/"))
        (catch clojure.lang.ExceptionInfo ex
          (let [problems (:problems (ex-data ex))
                full-params (merge tasting (:params req))]
            (render req views/edit-tasting-page full-params coffees problems)))))))

(defn create [req]
  (render req views/add-tasting-page (:params req) (get-coffees) []))

(defn save-new [req]
  (let [coffees (get-coffees)]
    (try
      (let [params (assoc (parse-params (forms/tasting-form coffees) (:params req))
                     :user_id (current-user-id req))]
        (let [tasting (create-tasting<! params)]
          (redirect req "/tasting/")))
      (catch clojure.lang.ExceptionInfo ex
        (let [problems (:problems (ex-data ex))]
          (render req views/add-tasting-page (:params req) coffees problems))))))

(defn delete [req]
  (when-let [tasting (get-own-tasting req)]
    (delete-tasting! (:id tasting))
    (redirect req "/tasting/")))
