(ns kahvipaivakirja.core
  (:use
   compojure.core
   kahvipaivakirja.views)
  (:require
   [compojure.handler :as handler]
   [compojure.route :as route]
   [hiccup.middleware :refer [wrap-base-url]]))

(defroutes main-routes
  (GET "/" [] (front-page))
  (GET "/coffee/" [] (coffee-ranking-page))
  (GET "/coffee/1/" [] (coffee-info-page))
  (GET "/coffee/1/edit/" [] (edit-coffee-page))
  (GET "/roastery/" [] (roastery-ranking-page))
  (GET "/roastery/1/" [] (roastery-info-page))
  (GET "/roastery/1/edit/" [] (edit-roastery-page))
  (GET "/tasting/" [] (new-tasting-page))
  (GET "/user/" [] (profile-page))
  (GET "/about" [] (readme))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/site main-routes)
      (wrap-base-url)))
