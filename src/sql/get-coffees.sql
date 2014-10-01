SELECT
	coffees.id as coffee_id,
	coffees.name as coffee_name,
	roasteries.id as roastery_id,
	roasteries.name as roastery_name,
	-- XXX(miikka) Is there a way to do this with only one sub-query?
	(SELECT AVG(rating) FROM tastings WHERE tastings.coffee_id = coffees.id) AS rating_avg,
	(SELECT COUNT(rating) FROM tastings WHERE tastings.coffee_id = coffees.id) AS rating_count
FROM coffees
INNER JOIN roasteries
ON roasteries.id = coffees.roastery_id
ORDER BY rating_avg DESC NULLS LAST;
