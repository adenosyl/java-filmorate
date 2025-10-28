package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final List<Film> films = new ArrayList<>();
    private int nextId = 1;

    @PostMapping
    public ResponseEntity<Film> createFilm(@RequestBody Film film) {
        validateFilm(film);
        if (film.getId() == 0) {
            film.setId(nextId++);
        }
        films.add(film);
        log.info("Создан фильм: {}", film);
        return ResponseEntity.ok(film);
    }

    @PutMapping
    public ResponseEntity<?> updateFilm(@RequestBody Film film) {
        validateFilm(film);
        for (int i = 0; i < films.size(); i++) {
            if (films.get(i).getId() == film.getId()) {
                films.set(i, film);
                log.info("Обновлен фильм: {}", film);
                return ResponseEntity.ok(film);
            }
        }
        log.error("Фильм с id = {} не найден", film.getId());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Фильм не найден");
        return ResponseEntity.status(404).body(error);
    }

    @GetMapping
    public ResponseEntity<List<Film>> getAllFilms() {
        return ResponseEntity.ok(films);
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации: Название фильма не может быть пустым");
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Ошибка валидации: Описание фильма не может превышать 200 символов");
            throw new ValidationException("Описание фильма не может превышать 200 символов");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Ошибка валидации: Дата релиза не может быть раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            log.error("Ошибка валидации: Продолжительность фильма должна быть положительной");
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }
}