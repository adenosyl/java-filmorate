package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.dao.MpaDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmDbStorage;

    @Autowired
    private GenreDbStorage genreDbStorage;

    @Autowired
    private MpaDbStorage mpaDbStorage;

    @Test
    void testAddFindUpdateDeleteFilmWithGenresAndMpa() {
        // Создаем фильм
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = mpaDbStorage.findById(1); // например рейтинг "G"
        film.setMpa(mpa);

        Genre genre1 = genreDbStorage.findById(1);
        Genre genre2 = genreDbStorage.findById(2);
        Set<Genre> genres = new HashSet<>();
        genres.add(genre1);
        genres.add(genre2);
        film.setGenres(genres);

        // Добавление
        Film addedFilm = filmDbStorage.add(film);
        assertThat(addedFilm.getId()).isNotNull();
        assertThat(addedFilm.getMpa().getId()).isEqualTo(mpa.getId());
        assertThat(addedFilm.getGenres()).hasSize(2);

        // Поиск
        Film foundFilm = filmDbStorage.findById(addedFilm.getId());
        assertThat(foundFilm.getName()).isEqualTo("Test Film");
        assertThat(foundFilm.getGenres()).hasSize(2);
        assertThat(foundFilm.getMpa().getId()).isEqualTo(mpa.getId());

        // Обновление - меняем имя и жанры
        foundFilm.setName("Updated Film");
        Set<Genre> newGenres = new HashSet<>();
        newGenres.add(genre1);
        foundFilm.setGenres(newGenres);
        filmDbStorage.update(foundFilm);

        Film updatedFilm = filmDbStorage.findById(foundFilm.getId());
        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
        assertThat(updatedFilm.getGenres()).hasSize(1);

        // Удаление
        filmDbStorage.delete(updatedFilm.getId());
        assertThrows(NotFoundException.class, () -> filmDbStorage.findById(updatedFilm.getId()));
    }
}
