DELETE FROM mpa_ratings;
DELETE FROM genres;
DELETE FROM film_directors;
DELETE FROM directors;
DELETE FROM films;

INSERT INTO mpa_ratings (id, name) VALUES (1, 'G');
INSERT INTO mpa_ratings (id, name) VALUES (2, 'PG');
INSERT INTO mpa_ratings (id, name) VALUES (3, 'PG-13');
INSERT INTO mpa_ratings (id, name) VALUES (4, 'R');
INSERT INTO mpa_ratings (id, name) VALUES (5, 'NC-17');

INSERT INTO genres (id, name) VALUES (1, 'Комедия');
INSERT INTO genres (id, name) VALUES (2, 'Драма');
INSERT INTO genres (id, name) VALUES (3, 'Мультфильм');
INSERT INTO genres (id, name) VALUES (4, 'Триллер');
INSERT INTO genres (id, name) VALUES (5, 'Документальный');
INSERT INTO genres (id, name) VALUES (6, 'Боевик');