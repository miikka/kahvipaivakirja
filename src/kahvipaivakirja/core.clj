(ns kahvipaivakirja.core
  (:use
   compojure.core
   kahvipaivakirja.model)
  (:require
   [cemerick.friend :as friend]
   [cemerick.friend.credentials :as creds]
   [cemerick.friend.workflows :as workflows]
   [compojure.handler :as handler]
   [compojure.route :as route]
   [hiccup.middleware :refer [wrap-base-url]]
   [kahvipaivakirja.views :as views]
   [ring.util.response :as response]))

;;; HELPERS

(defn current-user
  "Return the username of the currently authenticated user."
  [req]
  (-> req friend/identity :current))

(defn make-context
  "Return a map of contextual information to be used by the views."
  [req]
  (let [user (friend/current-authentication req)]
   {:user user
    :username (:username user)}))

(defn authenticated?
  "Returns true if the user has been authenticated."
  [req]
  (not (nil? (current-user req))))

(defn redirect
  "Redirect to the given relative path."
  [req path]
  (response/redirect (str (:context req) path)))

(defn render
  "Render the given view with the context derived from the given request."
  [req view & args]
  (let [ctx (make-context req)]
    (apply view ctx args)))

;;; CONTROLLERS

(defn login-page
  "The login page. If the user is already authenticated, they should
  be redirected to their user page."
  [req]
  (if (authenticated? req)
    (redirect req "/user/")
    (render req views/login-page
            (get-in req [:params :login_failed])
            (get-in req [:params :username] ""))))

;;; ROUTES

(defroutes main-routes
  (GET "/" req (render req views/front-page))
  (GET "/coffee/" req (render req views/coffee-ranking-page (get-coffees)))
  (GET "/coffee/:id/" req (render req views/coffee-info-page))
  (GET "/coffee/:id/edit/" req (render req views/edit-coffee-page))
  (GET "/roastery/" req (render req views/roastery-ranking-page))
  (GET "/roastery/:id/" req (render req views/roastery-info-page))
  (GET "/roastery/:id/edit/" req (render req views/edit-roastery-page))
  (GET "/tasting/" req (render req views/new-tasting-page))
  (GET "/user/" req (friend/authenticated (render req views/profile-page)))
  (GET "/login/" req (login-page req))
  (GET "/logout/" req (friend/logout* (redirect req "/")))
  (GET "/about" req (render req views/readme))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> main-routes
      (friend/authenticate
       {:credential-fn (partial creds/bcrypt-credential-fn (partial get-user-by-name))
        :login-uri "/login/"
        :workflows [(workflows/interactive-form)]})
      (handler/site)
      (wrap-base-url)))
