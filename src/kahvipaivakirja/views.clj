(ns kahvipaivakirja.views
  "Hiccup views for Kahvipäiväkirja"
  (:require
   [clojure.java.io :as io]
   [endophile.core :refer [mp to-clj html-string]]
   [hiccup.core :refer [html]]
   [hiccup.element :refer [link-to]]
   [hiccup.page :refer [html5 include-css include-js]]))

(defn ^:private include-bootstrap []
  (list (include-css "/bootstrap/css/bootstrap.css"
                     "/bootstrap/css/bootstrap-theme.css")
        (include-js "/bootstrap/js/bootstrap.js")))

(defn ^:private active?
  [page current & attr-map]
  (if (= page current)
    (merge {:class "active"} attr-map)
    attr-map))

(defn ^:private base [page title & content]
  (html5 {:lang "fi"}
   [:head
    [:title (if title
              (str title " - Kahvipäiväkirja")
              "Kahvipäiväkirja")]
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    (include-bootstrap)]
   [:body
    [:nav.navbar.navbar-default {:role "navigation"}
     [:div.container-fluid
      [:div.navbar-header
       [:button {:type "button" :class "navbar-toggle collapsed"
                 :data-toggle "navbar"}
        [:span.sr-only "Toggle navigation"]
        [:span.icon-bar]
        [:span.icon-bar]
        [:span.icon-bar]]
       [:a.navbar-brand {:href "#"} "Kahvipäiväkirja"]]
      [:div#navbar {:class "collapse navbar-collapse"}
       [:ul.nav.navbar-nav
        [:li (active? :front-page page) (link-to "/" "Etusivu")]]]]]
    [:div.container content]]))

(defn front-page []
  (base :front-page "Etusivu"
   [:h1 "Tervetuloa kahvipäiväkirjaan"]))

(defn readme
  "Render README.md as HTML."
  []
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
