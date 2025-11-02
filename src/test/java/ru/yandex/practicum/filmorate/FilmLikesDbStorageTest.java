package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FilmLikesDbStorageTest {

    @Autowired
    private FilmDbStorage filmDbStorage;

    @Autowired
    private UserDbStorage userDbStorage;

    @Test
    void testAddAndRemoveLike() {
        // Создаем пользователя
        var user = new ru.yandex.practicum.filmorate.model.User();
        user.setEmail("user@like.test");
        user.setLogin("userlike");
        user.setName("User Like");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user = userDbStorage.add(user);

        // Создаем фильм
        var film = new Film();
        film.setName("Film For Like Test");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        film = filmDbStorage.add(film);

        // Проверяем, что лайков нет
        assertTrue(film.getLikes().isEmpty());

        // Добавляем лайк
        filmDbStorage.addLike(film.getId(), user.getId());

        // Проверяем, что лайк добавлен
        Film filmFromDb = filmDbStorage.findById(film.getId());
        assertTrue(filmFromDb.getLikes().contains(user.getId()));

        // Удаляем лайк
        filmDbStorage.removeLike(film.getId(), user.getId());

        // Проверяем, что лайк удален
        filmFromDb = filmDbStorage.findById(film.getId());
        assertFalse(filmFromDb.getLikes().contains(user.getId()));

        // Очистка
        filmDbStorage.delete(film.getId());
        userDbStorage.delete(user.getId());
    }
}