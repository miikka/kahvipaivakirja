SELECT 
       roasteries.id AS roastery_id,
       roasteries.name AS roastery_name,
       (SELECT COUNT(*) FROM coffees WHERE coffees.roastery_id = roasteries.id) AS coffee_count,
       (SELECT AVG(rating)
        FROM tastings INNER JOIN coffees ON tastings.coffee_id = coffees.id
        WHERE coffees.roastery_id = roasteries.id) as rating_avg
FROM roasteries
ORDER BY rating_avg DESC NULLS LAST

