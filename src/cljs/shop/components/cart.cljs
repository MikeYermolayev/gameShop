(ns shop.cart
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [shop.sockets :refer [chsk-send!]]
            [shop.state :refer [user]]
            [shop.cart.dsl :refer [getCart removeFromCart clearCart]]))

(defn display [show]
  (if show
    #js {:display "block"}
    #js {:display "none"}))

(defn cartItem
  [state owner {:keys [cart-owner]}]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {
        :className "cart-item"
        }
        (dom/span #js {
          :className "cart-item-name"
          } str "title: " (:name state))
        (dom/span #js {
          :className "cart-item-count"
          } str "count: " (:count state))
        (dom/span #js {
          :className "cart-item-price"
          } str (:price state) "$")
        (dom/button #js {
          :className "remove-from-cart"
          :onClick (fn [e]
            (removeFromCart (:name state))(om/set-state! cart-owner [:cart] (getCart))
            )
          } "Remove")))))

(defn cart
  [state owner]
  (reify
    om/IInitState
    (init-state [_]
    {
      :cart (getCart)
      })
    om/IRenderState
    (render-state [_ {:keys [cart]}]
      (dom/div #js {
        :style (display (:isCartShown state))
        :className "cart"
        :ref "cart"
        }
        (dom/div #js {
          :className "fa fa-times"
          :onClick (fn [e]
            (om/update! state [:isCartShown] false))
          })
        (dom/div #js {
          :className "cart-items-container"
          }
          (om/build-all cartItem cart {:opts {:cart-owner owner}})
          )
        (dom/div #js {
          :className "confirm-buy"
          :onClick (fn [e]
            (chsk-send! [:transact/money {:client-balance (:bill (user)) :receipt (reduce + (map (fn [item] (* (:count item)(:price item))) cart))}]
                        3000
                        (fn [money-last]
                          (when (integer? money-last)
                            (om/update! (user) [:bill] money-last)
                            (shop.cart.dsl/clearCart)
                            (om/set-state! owner [:cart] (getCart))
                            )
                          )))
        }
        "Confirm buy")))))

