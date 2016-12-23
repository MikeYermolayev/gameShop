(ns shop.cart.dsl
	(:require [shop.ls :as ls]))

(def ls-key "cart")

(defn init-cart []
  (when (= nil (ls/get-item ls-key))
    (ls/set-item! ls-key [])))

(defn find-first [f seq]
         (first (filter f seq)))

(defn create-count-incrementer [[key val]]
  (fn [item]
    (if (= val (key item))
      (update-in item [:count] inc)
      item)))

(defn create-count-decrement [[key val]]
  (fn [item]
    (if (= val (key item))
      (update-in item [:count] dec)
      item)))

(defn getCart []
  (ls/get-item ls-key))

(defn addToCart [name price]
  (ls/set-item! ls-key (when-let [cart (ls/get-item ls-key)]
    (if-let [item (find-first #(= (:name %) name) cart)]
      (map (create-count-incrementer [:name name]) cart)
      (conj cart {:name name :price price :count 1})
    ))))

(defn removeFromCart [name]
  (ls/set-item! ls-key (when-let [cart (ls/get-item ls-key)]
    (println (find-first #(= (:name %) name) cart))
    (if-let [item (find-first #(= (:name %) name) cart)]
      (if (= 1 (:count item))
        (remove #(= (:name %) name) cart)
        (map (create-count-decrement [:name name]) cart))
      (remove #(= (:name %) name) cart)
    ))))

(defn clearCart []
  (ls/set-item! ls-key "[]"))
