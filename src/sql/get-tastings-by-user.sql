SELECT
	tastings.id,
	tastings.created,
	tastings.rating,
	roasteries.id as roastery_id,
	roasteries.name as roastery_name,
	coffees.id as coffee_id,
	coffees.name as coffee_name
FROM tastings
INNER JOIN coffees
ON coffees.id = tastings.coffee_id
INNER JOIN roasteries
ON roasteries.id = coffees.roastery_id
WHERE tastings.user_id = ?
ORDER BY tastings.created DESC

