(ns shop.home
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ajax.core :refer [GET POST json-response-format]]
            [secretary.core :as sec
                :include-macros true]
            [shop.state]
            [shop.ls :as ls]
            [shop.basket]
            [shop.sockets]))


(defn error-handler [{:keys [status status-text]}]
  (.log js/console "something bad happened:"))



(defn select-all

  ([selector]  (.querySelector js/document selector))
)

(defn game-view
    [game]
    (reify
        om/IRender
        (render [_]
                (dom/div #js {:className "item" :id (str "game-" (:gameid game))} 
                      (dom/div #js{:className "button-info"}
                        (when (:isadmin (shop.state/user))
                            (dom/i #js {:className "fa fa-edit"})
                        )
                        (when (:isadmin (shop.state/user))
                            (dom/i #js {:className "fa fa-trash" :onClick (fn[e] 
                                (let [id (last (str/split (.-id (select-all (str "#game-" (:gameid game)))) "game-") )]
                                  (println (shop.state/games))
                                  (POST "removeGame" {:format :json
                                                  :response-format (json-response-format {:keywords? true})
                                                  :params {:id id}
                                                  :handler (fn [response] 
                                                      (om/update! (shop.state/global-state) [:games] (filter (fn[item] (not= id (str (:gameid item))  )  ) (shop.state/games))  )
                                                      (om/update! (shop.state/global-state) [:allGames] (filter (fn[item] (not= id (str (:gameid item)) )  ) (shop.state/allGames))  )
                                                    )})
                                  )
                              )})
                        )                          
                        (dom/i #js {:className "fa fa-shopping-basket"
                                    :onClick (fn [e] 
                                      (shop.sockets/chsk-send! [:transact/money {:data (:price game)}] 
                                        3000
                                        (fn [e]
                                          (println e)
                                        )
                                      )
                                    )
                                  }
                        )
                        )
                      (dom/div #js{:className "item-info"} 
                          (dom/div #js {:className "item-year"} (dom/i nil "year : ")(:year game))
                          (dom/div #js {:className "item-genre"} (dom/i nil "genre : ")(:genre game))
                          (dom/div #js {:className "item-country"} (dom/i nil "country : ")(:country game))
                        )
                      (dom/div #js {:className "item-name"} (:name game))
                      (dom/div #js {:className "item-price"} (:price game) "$")
                  )

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

(defn item-view
    [item]
    (reify
        om/IRender
        (render [_]
                (dom/option #js {:className "item" :value (:id item)} (:name item) )

        )
    )
)

(defn select-list-view
    [items owner ownName]
    (reify
        om/IRender
        (render [_]
            (dom/select #js{:className (:name ownName)} (om/build-all item-view items))
        )
    )
)

(defn genre-view
    [item]
    (reify
        om/IRender
        (render [_]
                (dom/div #js {:className "genre-sidenav" :onClick (fn[e] 
                      (if (.contains (.-classList (.-target e)) "selected")
                        (.remove (.-classList (.-target e)) "selected")
                        (.add (.-classList (.-target e)) "selected"))
                      (let  [value (.-innerHTML (.-target e)) ] 
                      (if (contains? (shop.state/filteredGenres) value )
                          (om/update! (shop.state/global-state) [:filteredGenres]   (disj (shop.state/filteredGenres) value))
                          (om/update! (shop.state/global-state) [:filteredGenres] (set(conj (shop.state/filteredGenres) value)))
                        )
                      (println (shop.state/filteredGenres))
                      (om/update! (shop.state/global-state) [:games] 
                        (filter (fn[item] (or (empty? (shop.state/filteredGenres))  (contains? (shop.state/filteredGenres) (:genre item) ) ) ) (shop.state/allGames))
                      )

                       
                      )
                      
                      ; )
                 )} (:name item) )

        )
    )
)

(defn genres-list-view
    [items]
    (reify
        om/IRender
        (render [_]
            (dom/div nil (om/build-all genre-view items))
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
        (dom/div #js{
            :className "sidenav"
          }
            (om/build genres-list-view (:genres state) )
          )
        (dom/header nil
            "Misha&Artur's Games Shop"
            (dom/i #js {
               :className "fa fa-sign-out"
               :href "#/login"
               :onClick (fn [e]
                            (ls/remove-item! "token")
                            (om/update! state [:user] {})
                            (sec/dispatch! "/login"))}
              )
          )
        (dom/div #js{:className "content"}
          (dom/div #js{
                :className "pre-head"
            } str "Welcome to the online game shop," (dom/span #js{:className "login-name"} (:username (:user state)))
            (dom/i #js {
               :className "fa fa-shopping-basket"
               :onClick (fn [e]
                          (om/update! state [:isBasketShown] true))
              })
            (om/build shop.basket/basket state)
            )
          (when (:isadmin (shop.state/user))
            (dom/div #js {:className "add-item-panel"}
                (dom/div nil 
                  (dom/input #js{:type "text" :ref "name" :placeholder "Name"})
                  (dom/input #js{:type "number" :ref "year" :placeholder "Year"})
                  (dom/input #js{:type "number" :ref "price" :placeholder "Price"})
                  (om/build select-list-view (:countries state) {:opts {:name "country"}})
                  (om/build select-list-view (:genres state) {:opts {:name "genre"}} )
                  (dom/button #js{
                            :onClick (fn [e]
                                (let [name (.-value (om/get-node owner "name"))
                                      year (.-value (om/get-node owner "year"))
                                      price (.-value (om/get-node owner "price"))
                                      countryId  (.-value (select-all ".country"))
                                      genreId  (.-value (select-all ".genre"))]
                                      (println countryId)
                                    (when (and(not= name "") (not= price "") (not= year ""))
                                      (POST "game" {:format :json
                                                  :response-format (json-response-format {:keywords? true})
                                                  :params {:price price :name name :year year :countryId countryId :genreId genreId}
                                                  :handler (fn [response] 
                                                    (println (first (:generated_key response)))
                                                    (let [newGame {:price price :gameid (:generated_key response) :name name :year year :countryId countryId :genreId genreId}]
                                                      (om/update! state [:games] (set(conj (shop.state/games) newGame)))
                                                      (om/update! state [:allGames] (set(conj (shop.state/allGames) newGame)))
                                                      )
                                                    )}))
                                    )
                               ) 
                      } "add")
                  )
              )
           )
          (dom/div #js{:className "content-inner"}
                (dom/input #js{:className "search-input" :type "text" :placeholder "Search by name"})
                (om/build games-list-view (:games state) )
            )
          )
      )
    )
  )
  )