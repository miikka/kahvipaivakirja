SELECT
	roasteries.id AS roastery_id,
	roasteries.name AS roastery_name,
	(SELECT COUNT(*) FROM tastings WHERE tastings.coffee_id IN (SELECT id FROM coffees WHERE coffees.roastery_id = roasteries.id)) AS tasting_count
FROM roasteries
WHERE roasteries.id = ?
