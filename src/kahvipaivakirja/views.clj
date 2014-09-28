(ns kahvipaivakirja.views
  "Hiccup views for Kahvipäiväkirja.

  The views are Clojure functions that return an HTML string. They all
  take a context map as the first parameter. (See kahvipaivakirja.core/make-context.)"
  (:require
   [clojure.java.io :as io]
   [endophile.core :refer [mp to-clj html-string]]
   [formative.core :as formative]
   [hiccup.core :refer [html]]
   [hiccup.element :refer [image link-to]]
   [hiccup.page :refer [html5 include-css include-js]]
   [hiccup.util :refer [to-uri]]
   [kahvipaivakirja.forms :as forms]
   [kahvipaivakirja.views.helpers :refer [format-date]]))

(defn ^:private include-bootstrap []
  (list (include-css "/bootstrap/css/bootstrap.css"
                     "/bootstrap/css/bootstrap-theme.css")
        (include-js "/js/jquery-2.1.1.min.js"
                    "/bootstrap/js/bootstrap.js")))

;;; BASE TEMPLATE

(defn ^:private active?
  [page current & attr-map]
  (if (= page current)
    (merge {:class "active"} attr-map)
    attr-map))

(defn ^:private base [ctx page title & content]
  (html5 {:lang "fi"}
   [:head
    [:title (if title
              (str title " - Kahvipäiväkirja")
              "Kahvipäiväkirja")]
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    (include-bootstrap)
    (include-js "/js/starclicker.js")
    (include-css "/css/starclicker.css")]
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
       [:a.navbar-brand {:href (to-uri "/")} "Kahvipäiväkirja"]]
      [:div#navbar {:class "collapse navbar-collapse"}
       [:ul.nav.navbar-nav
        [:li (active? :front-page page) (link-to "/" "Etusivu")]
        [:li (active? :coffee-ranking page) (link-to "/coffee/" "Parhaat kahvit")]
        [:li (active? :roastery-ranking page) (link-to "/roastery/" "Parhaat paahtimot")]]
       [:ul.nav.navbar-nav.navbar-right
        (if (:user ctx)
          (list
           [:li (active? :new-tasting page) (link-to "/tasting/" "Lisää maistelu")]
           [:li (active? :profile page) (link-to "/user/" "Oma sivu")]
           [:li (link-to "/logout/" "Kirjaudu ulos")])
          [:li (link-to "/login/" "Kirjaudu sisään")])]]]]
    [:div.container content]]))

;;; HTML COMPONENTS (a.k.a. partials)

(defn ^:private input [id type label & [value]]
  [:div.form-group
   [:label {:for id} label]
   [:input {:id id :name id :type type :class "form-control" :value value}]])

(defn ^:private text-area [id type label]
  [:div.form-group
   [:label {:for id} label]
   [:textarea {:id id :class "form-control"}]])

(defn ^:private select [id label options]
  [:div.form-group
   [:label {:for id} label]
   [:select {:id id :class "form-control"}
    (for [[text value] options] [:option {:value value} text])]])

(defn ^:private empty-star []
  [:span.glyphicon.glyphicon-star-empty])

(defn ^:private submit-button [text]
  [:button {:type "submit" :class "btn btn-default"} text])

(defn ^:private link-button [target text]
  [:a {:href (to-uri target) :class "btn btn-primary" :role "button"} text])

(defn ^:private coffee-link
  [coffee & args]
  (apply link-to (str "/coffee/" (coffee :coffee_id) "/") (coffee :coffee_name)))

(defn ^:private roastery-link
  [coffee & args]
  (apply link-to (str "/roastery/" (coffee :roastery_id) "/") (coffee :roastery_name)))

(defn ^:private login-form
  [username]
  [:div.panel.panel-default
   [:div.panel-heading [:h3.panel-title "Kirjaudu"]]
   [:div.panel-body
    [:form {:role "form" :method "POST" :action (to-uri "/login/")}
     (list (input "username" :text "Käyttäjänimi" username)
           (input "password" :password "Salasana"))
     [:button {:type "submit" :class "btn btn-default"} "Kirjaudu"]]]])

(defn ^:private starclicker
  [name value]
  [:div.js-starclicker
   [:input {:type "hidden" :name name :value value}]
   (for [i (range 5)]
     [:span {:class (str "glyphicon glyphicon-star" (when (>= i value) "-empty"))
             :data-st-value (str (inc i))
             :role "button"}])])

;;; PAGES

(defn front-page [ctx]
  (base
   ctx :front-page "Etusivu"
   [:div.row
    [:div.col-md-6
     (image {:class "img-responsive"} "/images/jaakahvi.jpg")]
    [:div.col-md-6
     (if (:user ctx)
       (str "Tervetuloa kahvipäiväkirjaan, " (:username ctx) "!")
       (login-form ""))]]
   [:div.row
    [:div.col-md-6
     [:h3 "Parhaat kahvit"]
     [:ol
      [:li "Hacienda la Esmeralda (Tim Wendelboe)"]]]
    [:div.col-md-6
     [:h3 "Parhaat paahtimot"]
     [:ol
      [:li "Tim Wendelboe"]]]]))

(defn login-page [ctx show-error username]
  (base
   ctx :login-page "Kirjaudu"
   [:div.row
    [:div.col-md-6
     (when show-error
       [:div.alert.alert-danger {:role "alert"} "Virheellinen käyttäjänimi tai salasana!"])
     (login-form username)]]))

(defn new-tasting-page [ctx coffees]
  (base
   ctx :new-tasting "Lisää maistelu"
   [:div.page-header [:h2 "Lisää maistelu"]]
   [:div.row
    [:div.col-md-12
     (formative/render-form (forms/tasting-form coffees))]]))

(defn coffee-info-page [ctx]
  (base
   ctx :coffee-info "Drop Coffee: Marimira"
   [:div.page-header [:h1 (list (link-to "/roastery/1/" "Drop Coffee") ": Marimira")]]
   [:div.row
    [:div.col-md-12
     [:p "Kahvia Marimira on maisteltu yhden kerran. Ensimmäinen kerta 13.9.2014."]
     (when (:admin ctx) (link-button "/coffee/1/edit/" "Muokkaa"))]]
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

(defn roastery-info-page [ctx]
  (base
   ctx :roastery-info "Drop Coffee"
   [:div.page-header [:h1 "Drop Coffee"]]
   [:div.row
    [:div.col-md-12
     [:p "Paahtimon Drop Coffee kahveja on maisteltu yhden kerran. Ensimmäinen kerta 13.9.2014."]
     (when (:admin ctx) (link-button "/roastery/1/edit/" "Muokkaa"))]]
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

(defn coffee-ranking-page [ctx coffees]
  (base
   ctx :coffee-ranking "Parhaat kahvit"
   [:div.page-header [:h2 "Parhaat kahvit"]]
   [:div.row
    [:div.col-md-12
     [:p "HUOM! Tämän sivun sisältö tulee tietokannasta, mutta kaikkien muiden sivujen sisältö on toistaiseksi kovakoodattu. Siksi alla olevat linkit eivät toimi oikein."]]]
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

(defn roastery-ranking-page [ctx]
  (base
   ctx :roastery-ranking "Parhaat paahtimot"
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

(defn profile-page [ctx tastings]
  (base
   ctx :profile "Käyttäjäsivu"
   [:div.page-header [:h2 "Käyttäjä: " (:username ctx)]]
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
       [:th "Arvosana"]
       [:th "Toiminnot"]]
      (for [tasting tastings]
        [:tr
         [:td (format-date (:created tasting))]
         [:td (roastery-link tasting)]
         [:td (coffee-link tasting)]
         [:td (:rating tasting)]
         [:td (link-to {:class "btn btn-xs btn-default" :role "button"}
                       (str "/tasting/" (:id tasting) "/edit/") "Muokkaa")]])]]]))

(defn render-form [form values]
  (formative/render-form (assoc form :values values)))

(defn edit-tasting-page [ctx coffees tasting]
  (base
   ctx :edit-tasting "Muokkaa maistelua"
   [:div.page-header [:h2 (format "Maistelukokemus: %s (%s)"
                                  (:coffee_name tasting)
                                  (:roastery_name tasting))]]
   [:div.row
    [:div.col-md-12 (render-form (forms/tasting-form coffees) tasting)]]))

(defn edit-coffee-page [ctx]
  (base
   ctx :edit-coffee "Muokkaa kahvia"
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

(defn edit-roastery-page [ctx]
  (base
   ctx :edit-roastery "Muookkaa paahtimoa"
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

;;; README

(defn readme
  "Render README.md as HTML."
  [ctx]
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
     ctx :about "Esittelysivu"
     [:div.row
      [:div.col-md-12 content]])))
