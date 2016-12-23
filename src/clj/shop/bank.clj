(ns shop.bank)

(def shop-account (ref 1000))

(defn handle-payment [client-balance receipt]
  (let [client-account (ref client-balance)]
    (dosync
      (alter shop-account + receipt)
      (alter client-account - receipt))
    @client-account))