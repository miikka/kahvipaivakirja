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
   [hiccup.middleware :refer [wrap-base-url]]))

(defroutes main-routes
  (GET "/" [] (front-page))
  (GET "/coffee/" [] (coffee-ranking-page (get-coffees db-spec)))
  (GET "/coffee/:id/" [id] (coffee-info-page))
  (GET "/coffee/:id/edit/" [id] (edit-coffee-page))
  (GET "/roastery/" [] (roastery-ranking-page))
  (GET "/roastery/:id/" [id] (roastery-info-page))
  (GET "/roastery/:id/edit/" [id] (edit-roastery-page))
  (GET "/tasting/" [] (new-tasting-page))
  (GET "/user/" [] (friend/authenticated (profile-page)))
  (GET "/about" [] (readme))
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
