(ns kahvipaivakirja.core
  (:use
   compojure.core
   kahvipaivakirja.model
   kahvipaivakirja.views)
  (:require
   [cemerick.friend :as friend]
   [cemerick.friend.credentials :as creds]
   [cemerick.friend.workflows :as workflows]
   [compojure.handler :as handler]
   [compojure.route :as route]
   [hiccup.middleware :refer [wrap-base-url]]
   [ring.util.response :as response]))

;;; HELPERS

(defn current-user
  "Return the username of the currently authenticated user."
  [req]
  (-> req friend/identity :current))

(defn make-context
  "Return a map of contextual information to be used by the views."
  [req]
  {:user (current-user req)})

;;; ROUTES

(defroutes main-routes
  (GET "/" req (front-page (make-context req)))
  (GET "/coffee/" req (coffee-ranking-page (make-context req) (get-coffees db-spec)))
  (GET "/coffee/:id/" req (coffee-info-page (make-context req)))
  (GET "/coffee/:id/edit/" req (edit-coffee-page (make-context req)))
  (GET "/roastery/" req (roastery-ranking-page (make-context req)))
  (GET "/roastery/:id/" req (roastery-info-page (make-context req)))
  (GET "/roastery/:id/edit/" req (edit-roastery-page (make-context req)))
  (GET "/tasting/" req (new-tasting-page (make-context req)))
  (GET "/user/" req (friend/authenticated (profile-page (current-user req))))
  (GET "/logout/" req (friend/logout* (response/redirect (str (:context req) "/"))))
  (GET "/about" req (readme (make-context req)))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> main-routes
      (friend/authenticate
       {:credential-fn (partial creds/bcrypt-credential-fn (partial get-user-by-name))
        :login-uri "/"
        :workflows [(workflows/interactive-form)]})
      (handler/site)
      (wrap-base-url)))
