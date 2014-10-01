(ns kahvipaivakirja.core
  (:use
   compojure.core
   kahvipaivakirja.model)
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

;;; ROUTES

(defroutes main-routes
  (GET "/" req (render req views/front-page))
  (GET "/coffee/" req (render req views/coffee-ranking-page (get-coffees)))
  (GET "/coffee/:id/" [id :as req]
       (let [coffee (get-coffee-by-id (Integer/valueOf id))
             tastings (get-tastings-by-coffee coffee)]
        (render req views/coffee-info-page coffee tastings)))
  (GET "/coffee/:id/edit/" req
       (friend/authorize #{:admin} (render req views/edit-coffee-page)))
  (GET "/roastery/" req (render req views/roastery-ranking-page (get-roasteries)))
  (GET "/roastery/:id/" req (render req views/roastery-info-page))
  (GET "/roastery/:id/edit/" req
       (friend/authorize #{:admin} (render req views/edit-roastery-page)))
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
