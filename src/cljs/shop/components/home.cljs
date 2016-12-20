(ns shop.home
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ajax.core :refer [GET POST json-response-format]]
            [secretary.core :as sec
                :include-macros true]
            [shop.ls :as ls]))

(defn homeView
  [state owner]
  (println state)
  (reify
    om/IRender
    (render [_]
      (dom/div nil
        (dom/header nil
          (dom/h5 nil str "Welcome to the online game shop, " (:username (:user state)))
          (dom/button #js {:href "#/login"
                          :onClick (fn [e]
                            (ls/remove-item! "token")
                            (om/update! state [:user] {})
                            (sec/dispatch! "/login"))} "Log out"))
        ))))