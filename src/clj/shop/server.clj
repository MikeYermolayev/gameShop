(ns shop.server
  (:require [clojure.java.io :as io]
            [compojure.core :refer [ANY GET PUT POST DELETE defroutes]]
            [compojure.route :refer [resources]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.util.response :refer [response redirect content-type]]
            [environ.core :refer [env]]
            [org.httpkit.server :as httpkit]
            [taoensso.sente :as sente]
            [clj-time.core :as time]
            [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]
            [taoensso.timbre    :as timbre :refer (tracef debugf infof warnf errorf)]
            [shop.db.core]
            [buddy.sign.jwt :as jwt]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.token :refer [jws-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [buddy.hashers :as hashers]
            [shop.dao.user :as userDao]
            [shop.dao.games :as gamesDao]
            [shop.bank])
  (:gen-class))

(let [{:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket! (get-sch-adapter) {})]

  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
)

(def secret "secret")

(defonce router (atom nil))

(defn ok [response] {:status 200 :body response})
(defn bad [response] {:status 400 :body response})

(defmacro dissoc-reversed
  [operand1 operand2]
  (list `dissoc operand2 operand1))

(defmulti -event-msg-handler
  :id)

(defn event-msg-handler
  [{:as ev-msg :keys [id ?data event ?reply-fn]}]
  (-event-msg-handler ev-msg))

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (debugf "Unhandled event: %s" event)
    (when ?reply-fn
      (?reply-fn {:umatched-event-as-echoed-from-from-server event}))))

(defmethod -event-msg-handler :transact/money
  [{:keys [?data ?reply-fn]}] (when ?reply-fn
    (?reply-fn (shop.bank/handle-payment 500 (:data ?data))))
  )

(defn stopRouter! []
  (when-let [stop-fn @router]
    (stop-fn)
    (reset! router nil)))

(defn startRouter! []
  (reset! router
    (sente/start-chsk-router! ch-chsk event-msg-handler)))

(startRouter!)

(defn login
  [request]
  (let [username (get-in request [:body :username])
        password (get-in request [:body :password])
        user (userDao/getUserByName username)
        valid? (if (= user nil)
          false
          (hashers/check password (:password user)))]
    (if valid?
      (let [claims {:user (keyword username)
                    :exp (time/plus (time/now) (time/seconds 3600))}
            token (jwt/sign claims secret {:alg :hs512})]
        (ok {:token token :user (dissoc user :password)}))
      (bad {:message "wrong password"}))))

(defn register
  [request]
  (let [username (get-in request [:body :username])
        password (get-in request [:body :password])
        user (userDao/getUserByName username)]
    (if (= user nil)
        (let [claims {:user (keyword username)
                      :exp (time/plus (time/now) (time/seconds 3600))}
              token (jwt/sign claims secret {:alg :hs512})
              newUser (userDao/insertNew {:username username :password (hashers/encrypt password)})]
        (ok {:token token :user (dissoc-reversed :password newUser)}))
      (bad {:message "user exists"}))))



(defn createGame   [request]
  (let [newGame {:name (get-in request [:body :name])
                :year (get-in request [:body :year])
                :price (get-in request [:body :price])
                :description (get-in request [:body :description])
                :countryId (get-in request [:body :countryId])
                :genreId (get-in request [:body :genreId])}
        game (gamesDao/insertGame  newGame)
        ]
    (if (= game nil)
      (bad {:message "problem"})
      (ok game)
    )  
  )
)

(defn removeGame [request] 
  (let [status (gamesDao/removeGame (get-in request [:body :id]) )]
  (ok {:status status})
  )
  )
(defn updateGame [request])

(defn getAllGenres [request]
    (let [genres (gamesDao/getAllGenres)]
      (if(= genres nil)
        (bad {:message "error"})
        (ok {:genres genres})
        )
      )
  )
(defn getAllCountries [request]
    (let [countries (gamesDao/getAllCountries)]
      (if(= countries nil)
        (bad {:message "error"})
        (ok {:countries countries})
        )
      )
  )

(defn getAllGames [request]
    (let [games (gamesDao/getAllGames)]
      (if(= games nil)
        (bad {:message "error"})
        (ok {:games games})
        )
      )
  )

(defn verify
  [request]
  (if-not (authenticated? request)
    (throw-unauthorized)
    (ok {:user (dissoc-reversed :password (userDao/getUserByName (:user (:identity request))))})))


(defroutes routes
  (GET  "/chsk" req (ring-ajax-get-or-ws-handshake req))
  (POST "/chsk" req (ring-ajax-post                req))
  (GET "/game" [] getAllGames)
  (GET "/countries" [] getAllCountries)
  (GET "/genres" [] getAllGenres)
  (POST "/verifyToken" [] verify)
  (GET "/*" _
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (io/input-stream (io/resource "public/index.html"))})
  (POST "/login" [] login)
  (POST "/game" [] createGame)
  (PUT "/game" [] updateGame)
  (POST "/removeGame" [] removeGame)
  (POST "/register" [] register)
  (resources "/")
  (resources "/react" {:root "react"}))


(def auth-backend (jws-backend {:secret secret :options {:alg :hs512}}))

(def http-handler
  (-> routes
      (wrap-defaults api-defaults)
      wrap-with-logger
      wrap-gzip
      (wrap-authorization auth-backend)
      (wrap-authentication auth-backend)
      (wrap-json-response {:pretty false})
      (wrap-json-body {:keywords? true :bigdecimals? true})
      ))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (httpkit/run-server http-handler {:port port :join? false})))
