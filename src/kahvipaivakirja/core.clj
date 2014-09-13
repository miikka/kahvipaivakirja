(ns kahvipaivakirja.core
  (:use
   compojure.core)
  (:require
   [endophile.core :refer [mp to-clj html-string]]
   [endophile.hiccup :refer [to-hiccup]]
   [clojure.java.io :as io]
   [compojure.handler :as handler]
   [compojure.route :as route]
   [hiccup.core :refer [html]]
   [hiccup.middleware :refer [wrap-base-url]]))

(defn readme []
  "Render README.md as HTML."
  (let [content (-> (io/resource "README.md")
                    (io/file)
                    (slurp)
                    (mp)
                    ;; XXX(miikka) Ideally we would use to-hiccup, but
                    ;; it does not seem to support reference links
                    ;; ([this][kind]).
                    (to-clj)
                    (html-string))]
    (html
     [:html
      [:head [:title "Esittelysivu - Kahvipäiväkirja"]]
      [:body content]])))

(defroutes main-routes
  (GET "/" [] (readme))
  (GET "/about" [] (readme))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> (handler/site main-routes)
      (wrap-base-url)))
