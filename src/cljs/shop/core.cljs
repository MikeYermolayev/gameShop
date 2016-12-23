(ns shop.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [secretary.core :as sec
                :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [accountant.core :as accountant]
            [shop.home :refer [homeView]]
            [shop.login]
            [ajax.core :refer [GET POST json-response-format]]
            [shop.state]
            [shop.ls]
            [shop.cart.dsl :refer [init-cart]])
  (:import goog.History))

(enable-console-print!)
(sec/set-config! :prefix "#")
(let [history (History.)
      navigation EventType/NAVIGATE]
  (goog.events/listen history
                     navigation
                     #(-> % .-token sec/dispatch!))
  (doto history (.setEnabled true)))


(sec/defroute login-page "/login" []
    (om/root shop.login/login-view
        shop.state/app-state
        {:target (.getElementById js/document "app")}))

(sec/defroute home-page "/home" []
  (om/root homeView
        shop.state/app-state
        {:target (.getElementById js/document "app")}))

(defn main []
  (init-cart)
  (let [token (shop.ls/get-item "token")]
    (if token
      (POST "/verifyToken" {:format :json
                            :response-format (json-response-format {:keywords? true})
                            :headers {"Authorization" (str "Token " token)}
                            :error-handler (fn [response]
                              (shop.ls/clear!)
                              (init-cart)
                              (-> js/document
                              .-location
                              (set! "#/login")))
                            :handler (fn [response]
                              (println response)
                              (om/update! (shop.state/global-state) [:user] (:user response))
                              (-> js/document
                                .-location
                                (set! "#/home")))})
      (-> js/document
        .-location
        (set! "#/login")))))
    

(main)
