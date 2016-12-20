(ns shop.login
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ajax.core :refer [GET POST json-response-format]]
            [secretary.core :as sec
                :include-macros true]
            [shop.ls :as ls]))

(defn auth
  [url state owner]
  (let [username (.-value (om/get-node owner "name"))
        password (.-value (om/get-node owner "pass"))]
      (when (and(not= username "") (not= password ""))
        (POST url {:format :json
                    :response-format (json-response-format {:keywords? true})
                    :params {:username username :password password}
                    :handler (fn [response] 
                      (ls/set-item! "token" (:token response))
                      (om/transact! state :user (fn [_]
                        (select-keys (:user response) [:username :isadmin])))
                      (sec/dispatch! "/home"))}))))

(defn login-view
  [state owner]
  (reify
    om/IRender
    (render [_]
      (dom/div nil
        (dom/h3 nil "User name:")
        (dom/input #js {:placeholder "user name" :ref "name"})
        (dom/h3 nil "Password")
        (dom/input #js {:placeholder "password" :ref "pass"})
        (dom/button #js {:onClick #(auth "/login" state owner)}
        "Login")
        (dom/button #js {:onClick #(auth "/register" state owner)}
        "Register")))))