package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTests {

    private FilmController filmController;

    @BeforeEach
    void setUp() {
        var filmStorage = new InMemoryFilmStorage();
        var userStorage = new InMemoryUserStorage();
        var filmService = new FilmService(filmStorage, userStorage);
        filmController = new FilmController(filmService);
    }

    @Test
    void createFilm_ValidData_Success() {
        Film film = new Film();
        film.setName("Test Movie");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film created = filmController.createFilm(film);
        assertNotNull(created);
        assertEquals("Test Movie", created.getName());
    }

    @Test
    void createFilm_EmptyName_ThrowsException() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> filmController.createFilm(film)
        );
        assertEquals("Название фильма не может быть пустым", ex.getMessage());
    }

    @Test
    void createFilm_TooLongDescription_ThrowsException() {
        Film film = new Film();
        film.setName("Test");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> filmController.createFilm(film)
        );
        assertEquals("Описание фильма не может превышать 200 символов", ex.getMessage());
    }

    @Test
    void createFilm_TooEarlyReleaseDate_ThrowsException() {
        Film film = new Film();
        film.setName("Test");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        film.setDuration(120);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> filmController.createFilm(film)
        );
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", ex.getMessage());
    }

    @Test
    void createFilm_NonPositiveDuration_ThrowsException() {
        Film film = new Film();
        film.setName("Test");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0);

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> filmController.createFilm(film)
        );
        assertEquals("Продолжительность фильма должна быть положительной", ex.getMessage());
    }
}