(ns shop.info
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn display [show]
  (if show
    #js {:display "flex"}
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
        (dom/h3 #js{:className "info-label"} (:name (:currentGame state)))
        (dom/table #js{:className "info-table"}
            (dom/tbody nil
                (dom/tr nil
                    (dom/td nil "price")
                    (dom/td nil  str (:price (:currentGame state)) "$")
                  )
                (dom/tr nil
                    (dom/td nil "year")
                    (dom/td nil (:year (:currentGame state)))
                  )
                (dom/tr nil
                    (dom/td nil "genre")
                    (dom/td nil (:genre (:currentGame state)))
                  )
                (dom/tr nil
                    (dom/td nil "country")
                    (dom/td nil (:country (:currentGame state)))
                  )
                (dom/tr nil
                    (dom/td nil "description")
                    (dom/td nil (:description (:currentGame state)))
                  )
              )
          )
        (dom/div #js {
          :className "fa fa-times"
          :onClick (fn [e]
            (om/update! state [:isInfoPopupShown] false))
          })))))