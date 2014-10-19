(ns kahvipaivakirja.util
  "A collection of helpers for controllers."
  (:require
   [cemerick.friend :as friend]
   [ring.util.response :as response]))

;; RESPONSE HELPERS

(defn make-context
  "Return a map of contextual information to be used by the views."
  [req]
  (let [user (friend/current-authentication req)]
   {:user user
    :username (:username user)
    :admin (:admin user)}))

(defn render
  "Render the given view with the context derived from the given request."
  [req view & args]
  (let [ctx (make-context req)]
    (apply view ctx args)))

(defn redirect
  "Redirect to the given relative path."
  [req path]
  (response/redirect (str (:context req) path)))

;; REQUEST HELPERS

(defn get-id
  "Get the ID param in the URL as an integer. Returns nil if it can't be parsed."
  [req]
  (try
    (Integer/valueOf (get-in req [:params :id]))
    (catch NumberFormatException ex
      nil)))

;; AUTHENTICATION

(defn current-user [req] (friend/current-authentication req))

(defn authenticated?
  "Returns true if the user has been authenticated."
  [req]
  (not (nil? (friend/current-authentication req))))
