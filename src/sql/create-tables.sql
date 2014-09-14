CREATE TABLE users
(
	id SERIAL PRIMARY KEY,
	username VARCHAR(255) UNIQUE,
	email VARCHAR(255),
	password VARCHAR(255),
	admin BOOLEAN,
	joined TIMESTAMP
);

CREATE TABLE roasteries
(
	id SERIAL PRIMARY KEY,
	name VARCHAR(255) UNIQUE
);

CREATE TABLE coffees
(
	id SERIAL PRIMARY KEY,
	name VARCHAR(255) UNIQUE,
	roastery_id INTEGER REFERENCES roasteries(id)
);

CREATE TABLE tastings
(
	id SERIAL PRIMARY KEY,
	type VARCHAR(255),
	location VARCHAR(255) NULL,
	rating INTEGER,
	notes VARCHAR(65535),
	created TIMESTAMP,
	coffee_id INTEGER REFERENCES coffees(id),
	user_id INTEGER REFERENCES users(id) ON DELETE CASCADE
);
