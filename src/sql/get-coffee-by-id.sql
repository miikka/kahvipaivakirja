SELECT
       coffees.id AS coffee_id,
       coffees.name AS coffee_name,
       roasteries.id AS roastery_id,
       roasteries.name AS roastery_name,
       (SELECT COUNT(*) FROM tastings WHERE tastings.coffee_id = coffees.id) AS tasting_count,
       (SELECT MIN(created) FROM tastings WHERE tastings.coffee_id = coffees.id) AS first_tasting
FROM coffees INNER JOIN roasteries ON roasteries.id = coffees.roastery_id
WHERE coffees.id = ?;
