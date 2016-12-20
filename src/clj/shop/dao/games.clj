(ns shop.dao.games
  (:require [shop.db.core :refer [db]]
            [clojure.java.jdbc :as j]))

(defn getAllGames [] 
  (j/query db
    ["select games.id, games.name, year, price, genres.name as genre, countries.name as country from games join genres using (genre_id) join countries using (country_id)"]))

(defn deleteGame [id]
  (j/query db 
  	["delete from games where id = ?" id]))

(defn updateGame[game]
   (j/query db 
  	["update users SET name = ?,year = ?,genres_id = ?, countries_id = ? where id = ?" (:name game) (:year game) (:genres_id game) (:countries_id game) (:id game)]))
