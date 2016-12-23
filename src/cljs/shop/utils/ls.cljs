(ns shop.ls
  (:require [cljs.reader :as reader]))

(defn set-item!
  [key val]
  (.setItem (.-localStorage js/window) key val)
  (println "SETED"))

(defn get-item
  [key]
  (when-let [item (.getItem (.-localStorage js/window) key)]
    (reader/read-string item)))

(defn remove-item!
  [key]
  (.removeItem (.-localStorage js/window) key))

(defn clear!
  []
  (.clear (.-localStorage js/window)))