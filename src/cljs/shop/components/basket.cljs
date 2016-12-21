(ns shop.basket
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn display [show]
  (if show
    #js {:opacity 1}
    #js {:opacity 0}))

(defn basket
  [state owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {
        :style (display (:isBasketShown state))
        :className "basket"
        :ref "basket"
        }
        (dom/div #js {
          :className "fa fa-times"
          :onClick (fn [e]
            (om/update! state [:isBasketShown] false))
          })))))