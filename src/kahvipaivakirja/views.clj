(ns kahvipaivakirja.views
  "Hiccup views for Kahvipäiväkirja"
  (:require
   [clojure.java.io :as io]
   [endophile.core :refer [mp to-clj html-string]]
   [hiccup.core :refer [html]]
   [hiccup.page :refer [include-css]]))

(defn ^:private include-bootstrap []
  (include-css "/bootstrap/css/bootstrap.css"
               "/bootstrap/css/bootstrap-theme.css"))

(defn front-page []
  (html
   [:head
    [:title "Kahvipäiväkirja"]
    (include-bootstrap)]
   [:body
    [:h1 "Tervetuloa Kahvipäiväkirjaan!"]]))

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
