(ns kahvipaivakirja.views
  "Hiccup views for Kahvipäiväkirja"
  (:require
   [clojure.java.io :as io]
   [endophile.core :refer [mp to-clj html-string]]
   [hiccup.core :refer [html]]
   [hiccup.element :refer [image link-to]]
   [hiccup.page :refer [html5 include-css include-js]]))

(defn ^:private include-bootstrap []
  (list (include-css "/bootstrap/css/bootstrap.css"
                     "/bootstrap/css/bootstrap-theme.css")
        (include-js "/js/jquery-2.1.1.min.js"
                    "/bootstrap/js/bootstrap.js")))

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
                 :data-toggle "collapse" :data-target "#navbar"}
        [:span.sr-only "Toggle navigation"]
        [:span.icon-bar]
        [:span.icon-bar]
        [:span.icon-bar]]
       [:a.navbar-brand {:href "#"} "Kahvipäiväkirja"]]
      [:div#navbar {:class "collapse navbar-collapse"}
       [:ul.nav.navbar-nav
        [:li (active? :front-page page) (link-to "/" "Etusivu")]
        [:li (active? :new-tasting page) (link-to "/tasting/" "Lisää maistelu")]]]]]
    [:div.container content]]))

(defn ^:private input [id type label]
  [:div.form-group
   [:label {:for id} label]
   [:input {:id id :type type :class "form-control"}]])

(defn front-page []
  (base
   :front-page "Etusivu"
   [:div.row
    [:div.col-md-6
     (image {:class "img-responsive"} "/images/jaakahvi.jpg")]
    [:div.col-md-6
     [:div.panel.panel-default
      [:div.panel-heading [:h3.panel-title "Kirjaudu"]]
      [:div.panel-body
       [:form {:role "form"}
        (list (input "login-username" :text "Käyttäjänimi")
              (input "login-password" :password "Salasana"))
        [:button {:type "submit" :class "btn btn-default"} "Kirjaudu"]]]]]]
   [:div.row
    [:div.col-md-6
     [:h3 "Parhaat kahvit"]
     [:ol
      [:li "Esmeralda (Tim Wendelboe)"]]]
    [:div.col-md-6
     [:h3 "Parhaat paahtimot"]
     [:ol
      [:li "Tim Wendelboe"]]]]))

(defn new-tasting-page []
  (base
   :new-tasting "Lisää maistelu"
   [:div.row
    [:div.col-md-12
     [:h3 "Lisää maistelu"]]]
   [:form {:role "form"}
    [:div.row
     [:div.col-md-4 (input "tasting-roastery" :text "Paahtimo")]
     [:div.col-md-4 (input "tasting-coffee" :text "Kahvi")]
     [:div.col-md-4 (input "tasting-location" :text "Sijainti")]]
    [:div.row
     [:div.col-md-6 (input "tasting-type" :text "Laatu")]
     [:div.col-md-6 (input "tasting-rating" :text "Arvio")]]
    [:div.row
     [:div.col-md-12
      [:button {:type "submit" :class "btn btn-default"} "Tallenna"]]]]))

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
