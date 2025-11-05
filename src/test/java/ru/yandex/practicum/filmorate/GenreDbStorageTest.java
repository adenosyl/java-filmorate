package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GenreDbStorageTest {

    @Autowired private GenreDbStorage genreDbStorage;

    @Test
    void testFindAllGenres() {
        List<Genre> genres = genreDbStorage.findAll();
        assertThat(genres).hasSize(6);
    }
}