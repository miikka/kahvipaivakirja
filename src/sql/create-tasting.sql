INSERT INTO tastings (type, location, rating, notes, created, coffee_id, user_id)
VALUES (:type, :location, :rating, :notes, current_timestamp, :coffee_id, :user_id)
