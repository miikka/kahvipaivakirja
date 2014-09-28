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
   [formative.parse :refer [parse-params]]
   [hiccup.middleware :refer [wrap-base-url]]
   [kahvipaivakirja.forms :as forms]
   [kahvipaivakirja.views :as views]
   [ring.util.response :as response]))

;;; HELPERS

(defn current-user [req] (friend/current-authentication req))

(defn make-context
  "Return a map of contextual information to be used by the views."
  [req]
  (let [user (friend/current-authentication req)]
   {:user user
    :username (:username user)
    :admin (:admin user)}))

(defn authenticated?
  "Returns true if the user has been authenticated."
  [req]
  (not (nil? (friend/current-authentication req))))

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

(defn save-tasting
  [req]
  (let [params (parse-params (forms/tasting-form (get-roasteries) (get-coffees)) (:params req))
        new-tasting (create-tasting params)]
    (prn :foo (:params req) new-tasting)
    (assert new-tasting)
    (redirect req (str "/tasting/" (:id new-tasting) "/edit/"))))

;;; ROUTES

(defroutes main-routes
  (GET "/" req (render req views/front-page))
  (GET "/coffee/" req (render req views/coffee-ranking-page (get-coffees)))
  (GET "/coffee/:id/" req (render req views/coffee-info-page))
  (GET "/coffee/:id/edit/" req
       (friend/authorize #{:admin} (render req views/edit-coffee-page)))
  (GET "/roastery/" req (render req views/roastery-ranking-page))
  (GET "/roastery/:id/" req (render req views/roastery-info-page))
  (GET "/roastery/:id/edit/" req
       (friend/authorize #{:admin} (render req views/edit-roastery-page)))
  (GET "/tasting/" req
       (let [roasteries (get-roasteries)
             coffees (get-coffees)]
         (friend/authenticated (render req views/new-tasting-page roasteries coffees))))
  (POST "/tasting/" req (friend/authenticated (save-tasting req)))
  (GET "/tasting/:id/edit/" [id :as req]
       (friend/authenticated
        ;; XXX(miikka) Should check whether user owns the tasting!
        ;; XXX(miikka) Should 404 when there's no tasting.
        (let [tasting (get-tasting-by-id (Integer/valueOf id))]
          (render req views/edit-tasting-page tasting))))
  (GET "/user/" req
       (friend/authenticated
        (let [tastings (get-tastings-by-user (current-user req))]
          (render req views/profile-page tastings))))
  (GET "/login/" req (login-page req))
  (GET "/logout/" req (friend/logout* (redirect req "/")))
  (GET "/about" req (render req views/readme))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> main-routes
      (friend/authenticate
       {:credential-fn (partial creds/bcrypt-credential-fn get-user-by-name)
        :login-uri "/login/"
        :workflows [(workflows/interactive-form)]})
      (handler/site)
      (wrap-base-url)))
