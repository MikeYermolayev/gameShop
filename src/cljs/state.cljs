 (ns shop.state
(:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
 	)
 (defonce app-state (atom {:user {}
                      :games []
                      :tempSearchValue nil
                      :allGames []
                      :genres []
                      :countries []}))
 (defn user []
    (om/ref-cursor (:user (om/root-cursor app-state))))
  (defn games []
    (om/ref-cursor (:games (om/root-cursor app-state))))
 (defn global-state []
    (om/ref-cursor (om/root-cursor app-state)))
