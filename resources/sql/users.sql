-- :name get-user-by-username :? :1
-- :doc find user by username
SELECT * FROM user
WHERE username = :username


-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO user
(username, password)
VALUES (:username, :password)