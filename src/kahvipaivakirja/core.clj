(ns kahvipaivakirja.core
  (:use
   compojure.core)
  (:require
   [clojure.java.io :as io]
   [compojure.handler :as handler]
   [compojure.route :as route]
   [hiccup.middleware :refer [wrap-base-url]]
   [markdown.core :refer [md-to-html-string]]))

(defn readme []
  "Return README.md rendered into HTML."
  (let [content (-> (io/resource "README.md")
                    (io/file)
                    (slurp)
                    (md-to-html-string))]
    content))

(defroutes main-routes
  (GET "/" [] (readme))
  (GET "/about" [] (readme))
  (route/not-found "Not Found"))

(def app
  (-> (handler/site main-routes)
      (wrap-base-url)))
