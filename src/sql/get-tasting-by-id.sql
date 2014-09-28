SELECT
	tastings.id,
	tastings.created,
	tastings.rating,
	coffees.id AS coffee_id,
	coffees.name AS coffee_name,
	roasteries.id AS roastery_id,
	roasteries.name AS roastery_name
FROM tastings
     INNER JOIN coffees ON coffees.id = tastings.coffee_id
     INNER JOIN roasteries ON roasteries.id = coffees.roastery_id
WHERE tastings.id = ?
