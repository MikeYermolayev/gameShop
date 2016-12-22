(ns shop.info
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn display [show]
  (if show
    #js {:display "block"}
    #js {:display "none"}))

(defn info
  [state owner]
  (reify  
    om/IRender
    (render [_]
      (dom/div #js {
        :style (display (:isInfoPopupShown state))
        :className "info"
        :ref "info"
        }
        (dom/div #js {
          :className "fa fa-times"
          :onClick (fn [e]
            (om/update! state [:isInfoPopupShown] false))
          })))))