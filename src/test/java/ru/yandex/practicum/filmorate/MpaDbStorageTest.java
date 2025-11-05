package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dao.MpaDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class MpaDbStorageTest {

    @Autowired private MpaDbStorage mpaDbStorage;

    @Test
    void testFindAllMpas() {
        List<Mpa> mpas = mpaDbStorage.findAll();
        assertThat(mpas).hasSize(5);
    }

    @Test
    void testFindById_ValidId() {
        Mpa mpa = mpaDbStorage.findById(1);
        assertThat(mpa).isNotNull();
        assertThat(mpa.getName()).isEqualTo("G");
    }

    @Test
    void testFindById_NotFound() {
        assertThrows(NotFoundException.class, () -> mpaDbStorage.findById(999));
    }
}