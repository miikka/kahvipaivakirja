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
        [:li (active? :coffee-ranking page) (link-to "/coffee/" "Parhaat kahvit")]
        [:li (active? :roastery-ranking page) (link-to "/roastery/" "Parhaat paahtimot")]
        [:li (active? :new-tasting page) (link-to "/tasting/" "Lisää maistelu")]]]]]
    [:div.container content]]))

(defn ^:private input [id type label]
  [:div.form-group
   [:label {:for id} label]
   [:input {:id id :type type :class "form-control"}]])

(defn ^:private text-area [id type label]
  [:div.form-group
   [:label {:for id} label]
   [:textarea {:id id :class "form-control"}]])

(defn ^:private select [id type label options]
  [:div.form-group
   [:label {:for id} label]
   [:select {:id id :class "form-control"}
    (for [option options] [:option option])]])

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
      [:li "Hacienda la Esmeralda (Tim Wendelboe)"]]]
    [:div.col-md-6
     [:h3 "Parhaat paahtimot"]
     [:ol
      [:li "Tim Wendelboe"]]]]))

(defn ^:private empty-star []
  [:span.glyphicon.glyphicon-star-empty])

(defn new-tasting-page []
  (base
   :new-tasting "Lisää maistelu"
   [:div.row
    [:div.col-md-12
     [:h3 "Lisää maistelu"]]]
   [:form {:role "form"}
    [:div.row
     [:div.col-md-4 (select "tasting-roastery" :text "Paahtimo" ["" "Tim Wendelboe" "Square Mile Coffee"])]
     [:div.col-md-4 (select "tasting-coffee" :text "Kahvi" [""  "Juhla Mokka" "Tumma Presidentti"])]
     [:div.col-md-4 (input "tasting-location" :text "Sijainti")]]
    [:div.row
     [:div.col-md-4 (select "tasting-type" :text "Laatu" ["" "Suodatin" "Cappuccino" "Espresso"])]
     [:div.col-md-4
      ;; XXX(miikka) JS to make this work TBD.
      [:div.form-group
       [:label "Arvio"]
       [:div.form-control (for [i (range 5)] (empty-star))]]]
     [:div.col-md-4 (text-area "tasting-notes" :text "Muistiinpanot")]]
    [:div.row
     [:div.col-md-12
      [:button {:type "submit" :class "btn btn-default"} "Tallenna"]]]]))

(defn coffee-ranking-page []
  (base
   :coffee-ranking "Parhaat kahvit"
   [:div.page-header [:h2 "Parhaat kahvit"]]
   [:table.table.table-hover
    [:tr
     [:th "Kahvi"]
     [:th "Paahtimo"]
     [:th "Arvosana"]]
    [:tr
     [:td "Hacienda la Esmeralda"]
     [:td "Tim Wendelboe"]
     [:td "5"]]
    [:tr
     [:td "Juhla Mokka"]
     [:td "Paulig"]
     [:td "2.7"]]]))

(defn roastery-ranking-page []
  (base
   :roastery-ranking "Parhaat paahtimot"
   [:div.page-header [:h2 "Parhaat paahtimot"]
    [:table.table.table-hover
     [:tr
      [:th "Paahtimo"]
      [:th "Kahveja"]
      [:th "Paras kahvi"]
      [:th "Yhteisarvosana"]]
     [:tr
      [:td "Tim Wendelboe"]
      [:td "1"]
      [:td "Hacienda la Esmeralda"]
      [:td "5"]]
     [:tr
      [:td "Paulig"]
      [:td "1"]
      [:td "Juhla Mokka"]
      [:td "2.7"]]]]))

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
    (base
     :about "Esittelysivu"
     [:div.row
      [:div.col-md-12 content]])))
