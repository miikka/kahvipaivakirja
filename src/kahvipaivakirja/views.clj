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
        [:li (active? :new-tasting page) (link-to "/tasting/" "Lisää maistelu")]
        [:li (active? :profile page) (link-to "/user/" "Oma sivu")]]]]]
    [:div.container content]]))

(defn ^:private input [id type label & [value]]
  [:div.form-group
   [:label {:for id} label]
   [:input {:id id :type type :class "form-control" :value value}]])

(defn ^:private text-area [id type label]
  [:div.form-group
   [:label {:for id} label]
   [:textarea {:id id :class "form-control"}]])

(defn ^:private select [id label options]
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
     [:div.col-md-4 (select "tasting-roastery" "Paahtimo" ["" "Tim Wendelboe" "Square Mile Coffee"])]
     [:div.col-md-4 (select "tasting-coffee" "Kahvi" [""  "Juhla Mokka" "Tumma Presidentti"])]
     [:div.col-md-4 (input "tasting-location" :text "Sijainti")]]
    [:div.row
     [:div.col-md-4 (select "tasting-type" "Laatu" ["" "Suodatin" "Cappuccino" "Espresso"])]
     [:div.col-md-4
      ;; XXX(miikka) JS to make this work TBD.
      [:div.form-group
       [:label "Arvio"]
       [:div.form-control (for [i (range 5)] (empty-star))]]]
     [:div.col-md-4 (text-area "tasting-notes" :text "Muistiinpanot")]]
    [:div.row
     [:div.col-md-12
      [:button {:type "submit" :class "btn btn-default"} "Tallenna"]]]]))

(defn ^:private submit-button [text]
  [:button {:type "submit" :class "btn btn-default"} text])

(defn ^:private link-button [target text]
  [:a {:href target :class "btn btn-primary" :role "button"} text])

(defn coffee-info-page []
  (base
   :coffee-info "Drop Coffee: Marimira"
   [:div.page-header [:h1 (list (link-to "/roastery/1/" "Drop Coffee") ": Marimira")]]
   [:div.row
    [:div.col-md-12
     [:p "Kahvia Marimira on maisteltu yhden kerran. Ensimmäinen kerta 13.9.2014."]
     (link-button "/roastery/1/edit/" "Muokkaa")]]
   [:div.row
    [:div.col-md-12
     [:h3 "Maisteluhistoria"]]]
   [:div.row
    [:div.col-md-12
     [:table.table.table-hover
      [:tr
       [:th "Päiväys"]
       [:th "Käyttäjä"]
       [:th "Arvosana"]]
      [:tr
       [:td "13.9.2014"]
       [:td "Miikka"]
       [:td "4"]]]]]))

(defn roastery-info-page []
  (base
   :roastery-info "Drop Coffee"
   [:div.page-header [:h1 "Drop Coffee"]]
   [:div.row
    [:div.col-md-12
     [:p "Paahtimon Drop Coffee kahveja on maisteltu yhden kerran. Ensimmäinen kerta 13.9.2014."]
     (link-button "/coffee/1/edit/" "Muokkaa")]]
    [:div.row
     [:div.col-md-12
      [:h3 "Kahvit"]]]
    [:div.row
     [:div.col-md-12
      [:table.table.table-hover
       [:tr
        [:th "Kahvi"]
        [:th "Keskiarvosana"]
        [:th "Maisteluja"]]
       [:tr
        [:td (link-to "/coffee/1/" "Marimira")]
        [:td "4"]
        [:td "1"]]]]]))

(defn ^:private coffee-link
  [coffee & args]
  (apply link-to (str "/coffee/" (coffee :roastery_id) "/") (coffee :name)))

(defn ^:private roastery-link
  [coffee & args]
  (apply link-to (str "/roastery/" (coffee :roastery_id) "/") (coffee :roastery_name)))

(defn coffee-ranking-page [coffees]
  (base
   :coffee-ranking "Parhaat kahvit"
   [:div.page-header [:h2 "Parhaat kahvit"]]
   [:table.table.table-hover
    [:tr
     [:th "Kahvi"]
     [:th "Paahtimo"]
     [:th "Arvosana"]
     [:th "Arvosteluja"]]
    (for [coffee coffees]
      [:tr
       [:td (coffee-link coffee)]
       [:td (roastery-link coffee)]
       [:td (if-let [rating (coffee :rating_avg)]
              (format "%.2f" rating)
              "-")]
       [:td (coffee :rating_count)]])]))

(defn roastery-ranking-page []
  (base
   :roastery-ranking "Parhaat paahtimot"
   [:div.page-header [:h2 "Parhaat paahtimot"]]
   [:table.table.table-hover
    [:tr
     [:th "Paahtimo"]
     [:th "Kahveja"]
     [:th "Paras kahvi"]
     [:th "Keskiarvosana"]]
    [:tr
     [:td "Tim Wendelboe"]
     [:td "1"]
     [:td "Hacienda la Esmeralda"]
     [:td "5"]]
    [:tr
     [:td (link-to "/roastery/1/" "Drop Coffee")]
     [:td "1"]
     [:td (link-to "/coffee/1/" "Marimira")]
     [:td "4"]]
    [:tr
     [:td "Paulig"]
     [:td "1"]
     [:td "Juhla Mokka"]
     [:td "2.7"]]]))

(defn profile-page []
  (base
   :profile "Käyttäjäsivu"
   [:div.page-header [:h2 "Käyttäjäsivu"]]
   [:div.row
    [:div.col-md-12 [:h3 "Omat suosikit"]]]
   [:div.row
    [:div.col-md-12
     [:ol
      [:li "Heart Coffee: Kenya Miiri"]
      [:li "Drop Coffee: Marimira"]]]]
   [:div.row
    [:div.col-md-12 [:h3 "Maisteluhistoria"]]]
   [:div.row
    [:div.col-md-12
     [:table.table.table-hover
      [:tr
       [:th "Päiväys"]
       [:th "Paahtimo"]
       [:th "Kahvi"]
       [:th "Arvosana"]]
      [:tr
       [:td "13.9.2014"]
       [:td (link-to "/roastery/1/" "Drop Coffee")]
       [:td (link-to "/coffee/1/" "Marimira")]
       [:td "4"]]
      [:tr
       [:td "13.9.2014"]
       [:td "Square Mile Coffee"]
       [:td "Magdalena"]
       [:td "3"]]]]]))

(defn edit-coffee-page []
  (base
   :edit-coffee "Muokkaa kahvia"
   [:div.page-header [:h2 "Muokkaa kahvia Marimira"]]
   [:form {:role "form"}
    [:div.row
     [:div.col-md-6 (input "coffee-name" :text "Nimi" "Marimira")]
     [:div.col-md-6 (select "coffee-roastery" "Valitse paahtimo"
                             ["Drop Coffee" "Tim Wendelboe" "Square Mile Coffee"])]]
    [:div.row [:div.col-md-12 (submit-button "Tallenna")]]]
   [:div.row
    [:div.col-md-12 [:h2 "Yhdistä toiseen kahviin"]]]
   [:form {:role "form"}
    [:div.row
     [:div.col-md-12 (select "coffee-merge-coffee" "Valitse kahvi"
                             ["Drop Coffee / Marimira" "Square Mile Coffee / Magdalena"])]]
    [:div.row [:div.col-md-12 (submit-button "Yhdistä")]]]))

(defn edit-roastery-page []
  (base
   :edit-roastery "Muookkaa paahtimoa"
   [:div.page-header [:h2 "Muokkaa paahtimoa Drop Coffee"]]
   [:form {:role "form"}
    [:div.row
     [:div.col-md-12 (input "roastery-name" :text "Nimi" "Drop Coffee")]]
    [:div.row [:div.col-md-12 (submit-button "Tallenna")]]]
   [:div.row
    [:div.col-md-12 [:h2 "Yhdistä toiseen paahtimoon"]]]
   [:form {:role "form"}
    [:div.row
     [:div.col-md-12 (select "roastery-merge-roastery" "Valitse paahtimo"
                             ["Drop Coffee" "Square Mile Coffee"])]]
    [:div.row [:div.col-md-12 (submit-button "Yhdistä")]]]))

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
