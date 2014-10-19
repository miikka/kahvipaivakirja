(ns kahvipaivakirja.forms
  "Kahvipäiväkirja uses formative to render forms and to validate form
  input. This namespace contains some formative extensions and descriptions of the forms."
  (:require
   [clojure.string :as string]
   formative.render
   formative.util
   [formative.core :as f]))

;;; FORMATIVE EXTENSIONS

(defn to-int
  [s]
  (when (and s (not (and (string? s) (string/blank? s))))
    (Integer/valueOf s)))

(defmethod formative.render/render-field :stars
  [field]
  (let [value (or (some-> (:value field) to-int) 0)]
    [:div.js-starclicker
     (f/render-field (assoc field :type :hidden) (:value field))
     (for [i (range 5)]
       [:span {:class (str "glyphicon glyphicon-star" (when (>= i value) "-empty"))
               :data-st-value (str (inc i))
               :role "button"}])]))

;; Based on formative's bootstrap3.cljx, which contains a broken bootstrap3 renderer.
;; <https://github.com/jkk/formative/blob/master/src/formative/render/bootstrap3.cljx>
(defmethod formative.render/render-form ::bootstrap3-horizontal
  [form-attrs fields opts]
  (let [fields-by-name (into {} (for [f fields] [(:name f) f]))]
    [:form (assoc (dissoc form-attrs :renderer)
             :class "form-horizontal"
             :role "form")
     (when-let [problems (seq (:problems opts))]
       [:div.alert.alert-danger {:role "alert"}
        [:ul
         (for [{:keys [keys msg]} problems
               :let [labels
                     (map #(formative.render/get-field-label (or (fields-by-name (name %))
                                                                 {:name  %})) keys)]
               :when msg]
           [:li [:strong (string/join ", " labels) ": "] msg])]])
     (for [field fields
           :let [field-id (formative.util/get-field-id field)
                 field (assoc field
                         :id field-id
                         :class (str "form-control "
                                     ))]]
       [:div {:class (str "form-group " (when (:problem field) "has-error "))}
        [:label {:for field-id :class "col-sm-2 control-label"} (:label field)]
        [:div.col-sm-10 (formative.render/render-field field)]])]))

;;; FORMS

(defn tasting-form
  [coffees]
  (let [format-label (fn [coffee] (str (:roastery_name coffee) " / " (:coffee_name coffee)))
        coffee-opts (map (juxt :coffee_id format-label) coffees)]
    {:fields [{:name "coffee_id", :label "Kahvi", :type :select, :datatype :int,
               :options coffee-opts, :placeholder "(valitse laatu)"}
              {:name "location", :label "Sijainti", :type :text}
              {:name "rating", :label "Arvosana", :type :stars, :datatype :int}
              {:name "type", :label "Laatu", :type :select,
               :options ["suodatin" "cappuccino" "espresso"]
               :placeholder "(valitse laatu)"}
              {:name "notes", :label "Muistiinpanot", :type :textarea}]
     :submit-label "Tallenna"
     :renderer ::bootstrap3-horizontal
     :validations
     [[:required [:coffee_id :rating] "kenttä ei saa olla tyhjä"]
      [:within 1 5 [:rating] "arvosanan on oltava välillä 1-5"]]}))

(defn coffee-form
  [roasteries]
  (let [roastery-opts (map (juxt :roastery_id :roastery_name) roasteries)]
    {:fields [{:name "coffee_name", :label "Nimi", :type :text}
              {:name "roastery_id", :label "Paahtimo", :type :select, :datatype :int,
               :options roastery-opts, :placeholder "(valitse paahtimo)"}]
     :submit-label "Tallenna"
     :renderer ::bootstrap3-horizontal
     :validations
     [[:required [:coffee_name :roastery_id] "kenttä ei saa olla tyhjä"]]}))

(defn roastery-form
  []
  {:fields [{:name "roastery_name" :label "Nimi" :type :text}]
   :submit-label "Tallenna"
   :renderer ::bootstrap3-horizontal
   :validations
   [[:required [:roastery_name] "kenttä ei saa olla tyhjä"]]})

(defn roastery-merge-form
  [roasteries]
  (let [roastery-opts (map (juxt :roastery_id :roastery_name) roasteries)]
    {:fields [{:name "roastery_id" :label "Kohdepaahtimo" :type :select :datatype :int
               :options roastery-opts :placeholder "(valitse paahtimo)"}]
     :submit-label "Yhdistä"
     :renderer ::bootstrap3-horizontal
     :action "../merge/"}))
