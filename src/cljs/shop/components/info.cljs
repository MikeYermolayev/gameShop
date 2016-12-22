(ns shop.info
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ajax.core :refer [GET POST json-response-format]]
            [secretary.core :as sec
                :include-macros true]
            [clojure.string :as str]
  )
)


(defn error-handler [{:keys [status status-text]}]
  (.log js/console "something bad happened:"))


(defn infoView
  [state owner id]
  (reify
    om/IDidMount
      (did-mount [_] )
    om/IRender
    (render [_]
      (dom/div nil "dfad")
    )
  )
  )