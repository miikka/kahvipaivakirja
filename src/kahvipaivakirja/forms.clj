(ns kahvipaivakirja.forms
  (:require
   formative.render
   formative.util
   [formative.core :as f]))

(defmethod formative.render/render-field :stars
  [field]
  (let [value (or (:value field) 0)]
    [:div.js-starclicker
     (f/render-field (assoc field :type :hidden))
     (for [i (range 5)]
       [:span {:class (str "glyphicon glyphicon-star" (when (>= i value) "-empty"))
               :data-st-value (str (inc i))
               :role "button"}])]))

;; Based on formative's bootstrap3.cljx, which contains a broken bootstrap3 renderer.
;; <https://github.com/jkk/formative/blob/master/src/formative/render/bootstrap3.cljx>
(defmethod formative.render/render-form ::bootstrap3-horizontal
  [form-attrs fields opts]
  [:form (assoc (dissoc form-attrs :renderer)
           :class "form-horizontal"
           :role "form")
   (for [field fields
         :let [field-id (formative.util/get-field-id field)
               field (assoc field
                       :id field-id
                       :class "form-control")]]
     [:div.form-group
      [:label {:for field-id :class "col-sm-2 control-label"} (:label field)]
      [:div.col-sm-10(formative.render/render-field field)]])])

(defn tasting-form
  [roasteries coffees]
  (let [roastery-opts (map (juxt :id :name) roasteries)
        coffee-opts (map (juxt :coffee_id :coffee_name) coffees)]
    {:fields [{:name "roastery_id", :label "Paahtimo", :type :select, :datatype :int,
               :options roastery-opts, :placeholder "(valitse paahtimo)"}
              {:name "coffee_id", :label "Kahvi", :type :select, :datatype :int,
               :options coffee-opts, :placeholder "(valitse laatu)"}
              {:name "location", :label "Sijainti", :type :text}
              {:name "rating", :label "Arvosana", :type :stars, :datatype :int}
              {:name "type", :label "Laatu", :type :select,
               :options ["suodatin" "cappuccino" "espresso"]
               :placeholder "(valitse laatu)"}
              {:name "notes", :label "Muistiinpanot", :type :textarea}]
     :submit-label "Tallenna"
     :renderer ::bootstrap3-horizontal}))
