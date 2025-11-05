package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmLikesDbStorageTest {

    @Autowired private FilmDbStorage filmDbStorage;
    @Autowired private UserDbStorage userDbStorage;

    @Test
    void testAddAndRemoveLike() {
        User user = new User();
        user.setEmail("user@like.test");
        user.setLogin("userlike");
        user.setName("User Like");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user = userDbStorage.add(user);

        Film film = new Film();
        film.setName("Film For Like Test");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        film = filmDbStorage.add(film);

        assertTrue(film.getLikes().isEmpty());

        filmDbStorage.addLike(film.getId(), user.getId());
        Film filmFromDb = filmDbStorage.findById(film.getId());
        assertTrue(filmFromDb.getLikes().contains(user.getId()));

        filmDbStorage.removeLike(film.getId(), user.getId());
        filmFromDb = filmDbStorage.findById(film.getId());
        assertFalse(filmFromDb.getLikes().contains(user.getId()));

        filmDbStorage.delete(film.getId());
        userDbStorage.delete(user.getId());
    }
}