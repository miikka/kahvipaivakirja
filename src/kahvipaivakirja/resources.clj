(ns kahvipaivakirja.resources
  (:use
   kahvipaivakirja.model)
  (:require
   [kahvipaivakirja.forms :as forms]
   [kahvipaivakirja.views :as views]))

;;; RESOURCE ABSTRACTION

(defprotocol Resource
  (update [this params] "Update this resource with the given parameters.")
  (delete [this])
  (exists? [this] "Return a truthy value if this resource exists in the DB.")
  (form-of [this] "Return a Formative form for the resource.")
  (id-of [this])
  (url-of [this] "Return the canonical URL for the resource.")
  (edit-view [this] "Return the edit view for the resource.")
  (display-view [this])
  (get-value [this]))

(defrecord Roastery [roastery_id]
  Resource
  (update [_ params] (update-roastery! roastery_id params))
  (delete [_] (delete-roastery! roastery_id))
  (exists? [_] (get-roastery-by-id roastery_id))
  (form-of [_] (forms/roastery-form))
  (id-of [_] roastery_id)
  (url-of [_] (format "/%d/" roastery_id))
  (edit-view [_] views/edit-roastery-page)
  (display-view [_] views/roastery-info-page)
  (get-value [this] (merge this (get-roastery-by-id roastery_id))))

(defn get-Roastery [roastery_id] (get-value (Roastery. roastery_id)))

