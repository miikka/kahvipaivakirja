SELECT
	coffees.id AS coffee_id,
	coffees.name AS coffee_name,
	(SELECT AVG(rating) FROM tastings WHERE tastings.coffee_id = coffees.id) AS rating_avg,
	(SELECT COUNT(rating) FROM tastings WHERE tastings.coffee_id = coffees.id) AS rating_count
FROM coffees
WHERE coffees.roastery_id = ?
ORDER BY rating DESC NULLS LAST
