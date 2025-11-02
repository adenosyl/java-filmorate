package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        if (rs.getDate("release_date") != null) {
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        }
        film.setDuration(rs.getInt("duration"));
        // likes/genres/mpa читаем по id фильма
        film.setLikes(getLikesIds(film.getId()));
        film.setMpa(getMpaByFilmId(film.getId()));
        film.setGenres(getGenresByFilmId(film.getId()));
        return film;
    };

    private final RowMapper<Genre> genreRowMapper = (rs, rowNum) -> {
        Genre genre = new Genre();
        genre.setId(rs.getInt("id"));
        genre.setName(rs.getString("name"));
        return genre;
    };

    private final RowMapper<Mpa> mpaRowMapper = (rs, rowNum) -> {
        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("id"));
        mpa.setName(rs.getString("name"));
        return mpa;
    };

    @Override
    public Film add(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            if (film.getReleaseDate() != null) {
                ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            } else {
                ps.setDate(3, null);
            }
            ps.setInt(4, film.getDuration());
            if (film.getMpa() != null) {
                ps.setInt(5, film.getMpa().getId());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new RuntimeException("Не удалось получить id вставленного фильма");
        }
        int id = key.intValue();
        film.setId(id);

        // genres
        updateFilmGenres(film);

        // likes empty set for new film
        film.setLikes(new HashSet<>());
        return findById(id);
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        int updated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null,
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());

        if (updated == 0) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }

        // обновляем жанры — сначала удаляем старые
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        updateFilmGenres(film);

        return findById(film.getId());
    }

    @Override
    public void delete(int id) {
        String sqlDeleteLikes = "DELETE FROM film_likes WHERE film_id = ?";
        jdbcTemplate.update(sqlDeleteLikes, id);
        String sqlDeleteGenres = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(sqlDeleteGenres, id);
        String sqlDeleteFilm = "DELETE FROM films WHERE id = ?";
        int deleted = jdbcTemplate.update(sqlDeleteFilm, id);
        if (deleted == 0) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
    }

    @Override
    public Film findById(int id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, id);
        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        return films.get(0);
    }

    @Override
    public List<Film> findAll() {
        String sql = "SELECT * FROM films";
        return jdbcTemplate.query(sql, filmRowMapper);
    }

    private Set<Integer> getLikesIds(int filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        List<Integer> ids = jdbcTemplate.queryForList(sql, Integer.class, filmId);
        return new HashSet<>(ids);
    }

    @Override
    public void addLike(int filmId, int userId) {
        // используем MERGE, чтобы не выбрасывать ошибку при повторной вставке
        String sql = "MERGE INTO film_likes (film_id, user_id) KEY(film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    private Mpa getMpaByFilmId(int filmId) {
        String sql = "SELECT m.id, m.name FROM mpa m JOIN films f ON m.id = f.mpa_id WHERE f.id = ?";
        List<Mpa> result = jdbcTemplate.query(sql, mpaRowMapper, filmId);
        return result.isEmpty() ? null : result.get(0);
    }

    private Set<Genre> getGenresByFilmId(int filmId) {
        // Жанры должны возвращаться в порядке возрастания id
        String sql = """
        SELECT g.id, g.name
        FROM genres g
        JOIN film_genres fg ON g.id = fg.genre_id
        WHERE fg.film_id = ?
        ORDER BY g.id
    """;
        // Используем LinkedHashSet, чтобы сохранить порядок
        return new LinkedHashSet<>(jdbcTemplate.query(sql, genreRowMapper, filmId));
    }

    private void updateFilmGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            // Удаляем старые жанры
            String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
            jdbcTemplate.update(deleteSql, film.getId());

            // Добавляем жанры без дубликатов и в порядке по id
            String insertSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            film.getGenres().stream()
                    .map(Genre::getId)
                    .distinct() // убираем дубли
                    .sorted()   // сортируем по id, чтобы порядок совпадал с тестами
                    .forEach(genreId -> jdbcTemplate.update(insertSql, film.getId(), genreId));
        }
    }
}
