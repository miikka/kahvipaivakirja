(ns kahvipaivakirja.core
  (:use
   compojure.core
   kahvipaivakirja.model
   kahvipaivakirja.views)
  (:require
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
  (GET "/user/" [] (profile-page))
  (GET "/about" [] (readme))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/site main-routes)
      (wrap-base-url)))
