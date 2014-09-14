-- Users
INSERT INTO users
(username, email, password, admin, joined)
VALUES
(
 'Testaaja',
 'test@example.com',
 -- The password is 'kofeiini'.
 --
 -- XXX(miikka) Would be nice to use pgcrypto here and do
 -- crypt('kofeiini', gen_salt('bf')), but pgcrypto isn't enabled by
 -- default.
 '$2a$11$l3sEPpwDuW0lJimh7oP1.uDvIO8p3njeSb7a3K7ArpyRJpNTt.Z.e',
 false,
 current_timestamp
);

INSERT INTO users
(username, email, password, admin, joined)
VALUES
(
 'Ylläpitäjä',
 'test@example.com',
 -- The password is 'kofeiini'.
 '$2a$11$Uf63kyWK.8Sl9FHXb4DM.udCZB9gzVeR.EC/u2yY5K6ZqSkCLIEqy',
 true,
 current_timestamp
);


-- Roasteries
INSERT INTO roasteries (name) VALUES ('Drop Coffee');
INSERT INTO roasteries (name) VALUES ('Square Mile Coffee');
INSERT INTO roasteries (name) VALUES ('Tim Wendelboe');
INSERT INTO roasteries (name) VALUES ('Paulig');


-- Coffees
INSERT INTO coffees (name, roastery_id)
VALUES ('Marimira', (SELECT id FROM roasteries WHERE name = 'Drop Coffee'));

INSERT INTO coffees (name, roastery_id)
VALUES ('Magdalena', (SELECT id FROM roasteries WHERE name = 'Square Mile Coffee'));

INSERT INTO coffees (name, roastery_id)
VALUES ('Hacienda la Esmeralda', (SELECT id FROM roasteries WHERE name = 'Tim Wendelboe'));

INSERT INTO coffees (name, roastery_id)
VALUES ('Juhla Mokka', (SELECT id FROM roasteries WHERE name = 'Paulig'));

-- Tastings
INSERT INTO tastings (type, location, rating, notes, created, coffee_id, user_id)
VALUES
(
 'suodatin',
 NULL,
 2,
 'Testidataa.',
 current_timestamp,
 (SELECT id FROM coffees WHERE name = 'Juhla Mokka'),
 (SELECT id FROM users WHERE username = 'Testaaja')
);

