(ns shop.dao.games
  (:require [shop.db.core :refer [db]]
            [clojure.java.jdbc :as j]))

(defn getAllGames [] 
  (j/query db
    ["select games.id as gameid, games.name, year, price, genres.name as genre, countries.name as country from games join genres using (genre_id) join countries using (country_id)"]))
(defn getAllGenres [] 
  (j/query db
    ["select genre_id as id, name from genres"]))
(defn getAllCountries [] 
  (j/query db
    ["select country_id as id, name from countries"]))
(defn removeGame [id]
 (j/delete! db :games ["id = ?" id])
 )

(defn insertGame [creds]
  (j/insert! db :games {:name (:name creds) :year (:year creds) :price (:price creds) :genre_id (:genreId creds) :country_id (:countryId creds)})
)

(defn updateGame[game]
   (j/query db 
  	["update users SET name = ?,year = ?,genres_id = ?, countries_id = ? where id = ?" (:name game) (:year game) (:genresId game) (:countriesId game) (:id game)]))
