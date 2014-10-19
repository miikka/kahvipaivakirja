(ns kahvipaivakirja.core
  (:use
   compojure.core
   kahvipaivakirja.model
   kahvipaivakirja.resources)
  (:require
   [cemerick.friend :as friend]
   [cemerick.friend.credentials :as creds]
   [cemerick.friend.workflows :as workflows]
   [compojure.handler :as handler]
   [compojure.route :as route]
   [formative.parse :refer [parse-params]]
   [hiccup.middleware :refer [wrap-base-url]]
   [kahvipaivakirja.forms :as forms]
   [kahvipaivakirja.views :as views]
   [ring.util.response :as response]))

;;; HELPERS

(defn current-user [req] (friend/current-authentication req))

(defn make-context
  "Return a map of contextual information to be used by the views."
  [req]
  (let [user (friend/current-authentication req)]
   {:user user
    :username (:username user)
    :admin (:admin user)}))

(defn authenticated?
  "Returns true if the user has been authenticated."
  [req]
  (not (nil? (friend/current-authentication req))))

(defn redirect
  "Redirect to the given relative path."
  [req path]
  (response/redirect (str (:context req) path)))

(defn render
  "Render the given view with the context derived from the given request."
  [req view & args]
  (let [ctx (make-context req)]
    (apply view ctx args)))

;;; CONTROLLERS

(defn login-page
  "The login page. If the user is already authenticated, they should
  be redirected to their user page."
  [req]
  (if (authenticated? req)
    (redirect req "/user/")
    (render req views/login-page
            (get-in req [:params :login_failed])
            (get-in req [:params :username] ""))))

(defn save-tasting
  [req]
  (let [coffees (get-coffees)
        user-id (:id (current-user req))]
    (try
      (let [params (assoc (parse-params (forms/tasting-form coffees) (:params req))
                     :user_id user-id)]
        (create-tasting params)
        (redirect req "/user/"))
      (catch clojure.lang.ExceptionInfo ex
        (let [problems (:problems (ex-data ex))]
          (render req views/new-tasting-page coffees (:params req) problems))))))

;; XXX(miikka) This function duplicates save-tasting quite a bit.
(defn update-tasting-
  [req tasting-id]
  (let [coffees (get-coffees)
        user-id (:id (current-user req))
        tasting (get-tasting-by-id tasting-id)]
    (when tasting
      (if (= (:user_id tasting) user-id)
       (try
         (let [params (parse-params (forms/tasting-form coffees) (:params req))]
           (update-tasting tasting-id params)
           (redirect req "/user/"))
         (catch clojure.lang.ExceptionInfo ex
           (let [problems (:problems (ex-data ex))]
             (render req views/edit-tasting-page coffees (:params req) problems))))
       (friend/throw-unauthorized (friend/identity req) {})))))

(defn delete-tasting
  [req id]
  ;; XXX(miikka) Ensure that the user owns this tasting.
  (let [tasting (get-tasting-by-id id)]
    (when tasting
      (if (= (:user_id tasting) (:id (current-user req)))
        (do
          (delete-tasting! id)
          (redirect req "/user/"))
        (friend/throw-unauthorized (friend/identity req) {})))))

(defn save-coffee
  [req]
  (let [roasteries (get-roasteries)]
   (try
     (let [params (assoc (parse-params (forms/coffee-form roasteries) (:params req)))]
       (create-coffee params)
       (redirect req "/coffee/"))
     (catch clojure.lang.ExceptionInfo ex
       (let [problems (:problems (ex-data ex))]
         (pr-str problems))))))

(defn update-coffee-
  [req coffee-id]
  ;; XXX(miikka) Should check if admin
  (let [roasteries (get-roasteries)
        coffee (get-coffee-by-id coffee-id)]
    (when coffee
      (try
        (let [params (parse-params (forms/coffee-form roasteries) (:params req))]
          (update-coffee! coffee-id params)
          (redirect req (format "/coffee/%d/" coffee-id)))
        (catch clojure.lang.ExceptionInfo ex
          (let [problems (:problems (ex-data ex))]
            (render req views/edit-coffee-page (:params req) roasteries problems)))))))

(defn delete-coffee
  [req coffee-id]
  ;; XXX(miikka) Should check if admin
  (let [coffee (get-coffee-by-id coffee-id)]
    (when coffee
      (delete-coffee! coffee-id)
      (redirect req "/coffee/"))))

;;; THE RESOURCE WAY OF DOING THINGS

(defn create-resource
  [req resource]
  (try
    (let [params (parse-params (form-of resource) (:params req))]
      (when-let [id (create resource params)]
        (redirect req (format "/%d/" (:id id)))))
    (catch clojure.lang.ExceptionInfo ex
      (let [problems (:problems (ex-data ex))]
        (render req (create-view resource) (:params req) problems)))))

(defn update-resource
  [req resource]
  (when (exists? resource)
    (try
      (let [params (parse-params (form-of resource) (:params req))]
        (update resource params)
        (redirect req (format "/%d/" (id-of resource))))
      (catch clojure.lang.ExceptionInfo ex
        (let [problems (:problems (ex-data ex))]
          (render req (edit-view resource) (:params req) problems))))))

(defn delete-resource
  [req resource]
  (when (exists? resource)
    (delete resource)
    (redirect req "/")))

(defn resource-route [getter]
  (let [g (fn [id] (getter (Integer/valueOf id)))]
    (routes
     (GET "/create/" req (render req (create-view (getter -1)) {} {}))
     (POST "/create/" req (create-resource req (getter -1)))
     (GET "/:id/edit/" [id :as req] (when-let [res (g id)] (render req (edit-view res) res {})))
     (POST "/:id/edit/" [id :as req] (update-resource req (g id)))
     (POST "/:id/delete/" [id :as req] (delete-resource req (g id))))))

;;; ROUTES

(defroutes main-routes
  (GET "/" req
       (let [roasteries (take 3 (get-roasteries))
             coffees (take 3 (get-coffees))]
         (render req views/front-page roasteries coffees)))
  (GET "/coffee/" req (render req views/coffee-ranking-page (get-coffees)))
  (GET "/coffee/:id/" [id :as req]
       (let [coffee (get-coffee-by-id (Integer/valueOf id))
             tastings (get-tastings-by-coffee coffee)]
        (render req views/coffee-info-page coffee tastings)))
  (GET "/coffee/:id/edit/" [id :as req]
       (let [coffee (get-coffee-by-id (Integer/valueOf id))
             roasteries (get-roasteries)]
         (when coffee
           (friend/authorize #{:admin} (render req views/edit-coffee-page coffee roasteries {})))))
  (POST "/coffee/" req (friend/authenticated (save-coffee req)))
  (POST "/coffee/:id/edit/" [id :as req]
        (friend/authenticated (update-coffee- req (Integer/valueOf id))))
  (POST "/coffee/:id/delete/" [id :as req]
        (friend/authenticated (delete-coffee req (Integer/valueOf id))))

  (GET "/roastery/" req (render req views/roastery-ranking-page (get-roasteries)))

  (context "/roastery" [] (resource-route get-Roastery))

  (GET "/roastery/:id/" [id :as req]
       (let [roastery (get-roastery-by-id (Integer/valueOf id))
             coffees (get-coffees-by-roastery roastery)]
         (render req views/roastery-info-page roastery coffees)))

  (GET "/tasting/" req
       (let [coffees (get-coffees)]
         (friend/authenticated (render req views/new-tasting-page coffees {} []))))
  (POST "/tasting/" req (friend/authenticated (save-tasting req)))
  (POST "/tasting/:id/edit/" [id :as req] (friend/authenticated (update-tasting- req (Integer/valueOf id))))
  (POST "/tasting/:id/delete/" [id :as req]
        (friend/authenticated (delete-tasting req (Integer/valueOf id))))
  (GET "/tasting/:id/edit/" [id :as req]
       (friend/authenticated
        ;; XXX(miikka) Should check whether user owns the tasting!
        (let [tasting (get-tasting-by-id (Integer/valueOf id))]
          (when tasting
            (render req views/edit-tasting-page (get-coffees) tasting)))))
  (GET "/user/" req
       (friend/authenticated
        (let [tastings (get-tastings-by-user (current-user req))]
          (render req views/profile-page tastings))))
  (GET "/login/" req (login-page req))
  (GET "/logout/" req (friend/logout* (redirect req "/")))
  (GET "/about" req (render req views/readme))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (-> main-routes
      (friend/authenticate
       {:credential-fn (partial creds/bcrypt-credential-fn get-user-by-name)
        :login-uri "/login/"
        :workflows [(workflows/interactive-form)]})
      (handler/site)
      (wrap-base-url)))
