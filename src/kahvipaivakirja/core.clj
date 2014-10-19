(ns kahvipaivakirja.core
  "The main namespace for Kahvipäiväkirja. Contains the routing table and authentication code."
  (:use
   compojure.core
   kahvipaivakirja.model
   kahvipaivakirja.util)
  (:require
   [cemerick.friend :as friend]
   [cemerick.friend.credentials :as creds]
   [cemerick.friend.workflows :as workflows]
   [compojure.handler :as handler]
   [compojure.route :as route]
   [hiccup.middleware :refer [wrap-base-url]]
   [kahvipaivakirja.views :as views]
   [kahvipaivakirja.controllers.coffee :as coffee]
   [kahvipaivakirja.controllers.roastery :as roastery]
   [kahvipaivakirja.controllers.tasting :as tasting]))

;;; MISC CONTROLLERS

(defn front-page
  "Render the front page."
  [req]
  (let [roasteries (take 3 (get-roasteries))
        coffees (take 3 (get-coffees))]
    (render req views/front-page roasteries coffees)))

(defn profile-page
  "Render user's profile page."
  [req]
  (let [tastings (get-tastings-by-user (current-user req))]
    (render req views/profile-page tastings)))

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
  (GET "/" req (front-page req))

  ;; COFFEE ROUTES

  (GET "/coffee/" req (coffee/list req))
  (GET "/coffee/create/" req (friend/authenticated (coffee/create req)))
  (GET "/coffee/:id/" req (coffee/display req))
  (GET "/coffee/:id/edit/" req (friend/authorize #{:admin} (coffee/edit req)))

  (POST "/coffee/create/" req (friend/authenticated (coffee/save-new req)))
  (POST "/coffee/:id/edit/" req (friend/authorize #{:admin} (coffee/save-edit req)))
  (POST "/coffee/:id/delete/" req (friend/authorize #{:admin} (coffee/delete req)))

  ;; ROASTERY ROUTES

  (GET "/roastery/" req (roastery/list req))
  (GET "/roastery/create/" req (friend/authenticated (roastery/create req)))
  (GET "/roastery/:id/" req (roastery/display req))
  (GET "/roastery/:id/edit/" req (friend/authorize #{:admin} (roastery/edit req)))

  (POST "/roastery/create/" req (friend/authenticated (roastery/save-new req)))
  (POST "/roastery/:id/edit/" req (friend/authorize #{:admin} (roastery/save-edit req)))
  (POST "/roastery/:id/delete/" req (friend/authorize #{:adimn} (roastery/delete req)))
  (POST "/roastery/:id/merge/" req (friend/authorize #{:admin} (roastery/merge req)))

  ;; TASTING ROUTES

  (GET "/tasting/" req (redirect req "/user/"))
  (GET "/tasting/create/" req (friend/authenticated (tasting/create req)))
  (GET "/tasting/:id/:edit/" req (friend/authenticated (tasting/edit req)))

  (POST "/tasting/create/" req (friend/authenticated (tasting/save-new req)))
  (POST "/tasting/:id/edit/" req (friend/authenticated (tasting/save-edit req)))
  (POST "/tasting/:id/delete/" req (friend/authenticated (tasting/delete req)))

  ;; USER ROUTES

  (GET "/user/" req (friend/authenticated (profile-page req)))
  (GET "/login/" req (login-page req))
  (GET "/logout/" req (friend/logout* (redirect req "/")))

  ;; MISC ROUTES

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
