(ns shop.ls
  (:require [cljs.reader :as reader]))

(defn set-item!
  [key val]
  (.setItem (.-localStorage js/window) key val))

(defn get-item
  [key]
  (reader/read-string (.getItem (.-localStorage js/window) key)))

(defn remove-item!
  [key]
  (.removeItem (.-localStorage js/window) key))