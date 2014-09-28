UPDATE tastings SET
	type = :type,
	location = :location,
	rating = :rating,
	notes = :notes,
	coffee_id = :coffee_id
WHERE
id = :id
