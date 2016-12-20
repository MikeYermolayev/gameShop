(ns shop.home
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ajax.core :refer [GET POST json-response-format]]
            [secretary.core :as sec
                :include-macros true]
            [shop.ls :as ls]))


(defn error-handler [{:keys [status status-text]}]
  (.log js/console "something bad happened:"))


(defn game-view
    [game]
    (reify
        om/IRender
        (render [_]
                (dom/div nil (:name game))
        )
    )
)

(defn games-list-view
    [games]
    (reify
        om/IRender
        (render [_]
            (dom/div nil
                (om/build-all game-view games)
            )
        )
    )
)

(defn homeView
  [state owner]
  (reify
    om/IDidMount
      (did-mount [_]
        (GET "/game" 
            {
            :response-format (json-response-format {:keywords? true})
            :handler ( fn[response]  (om/update! state [:games] (:games response))  ) 
            :error-handler error-handler}
            )
        )
    om/IRender
    (render [_]
      (dom/div nil
        (dom/header nil
          (dom/div nil
                (om/build games-list-view (:games state))
          )
        )
      )
    )
  )
  )