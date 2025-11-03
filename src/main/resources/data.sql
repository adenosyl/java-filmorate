DELETE FROM film_likes;
DELETE FROM film_genres;
DELETE FROM friends;
DELETE FROM films;
DELETE FROM users;
DELETE FROM genres;
DELETE FROM mpa;

ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
ALTER TABLE films ALTER COLUMN id RESTART WITH 1;

MERGE INTO genres (id, name) KEY(id) VALUES
    (1, 'Комедия'),
    (2, 'Драма'),
    (3, 'Мультфильм'),
    (4, 'Триллер'),
    (5, 'Документальный'),
    (6, 'Боевик');

MERGE INTO mpa (id, name) KEY(id) VALUES
    (1, 'G'),
    (2, 'PG'),
    (3, 'PG-13'),
    (4, 'R'),
    (5, 'NC-17');