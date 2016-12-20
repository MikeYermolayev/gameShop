(ns shop.ls)

(defn set-item!
  [key val]
  (.setItem (.-localStorage js/window) key val))

(defn get-item
  [key]
  (.getItem (.-localStorage js/window) key))

(defn remove-item!
  [key]
  (.removeItem (.-localStorage js/window) key))