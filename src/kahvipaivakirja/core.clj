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
  (GET "/tasting/" [] (new-tasting-page))
  (GET "/about" [] (readme))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/site main-routes)
      (wrap-base-url)))
