SELECT
	tastings.id AS tasting_id,
	tastings.created,
	tastings.rating,
	users.id AS user_id,
	users.username AS user_name
FROM tastings
     INNER JOIN users ON tastings.user_id = users.id
WHERE tastings.coffee_id = ?;
