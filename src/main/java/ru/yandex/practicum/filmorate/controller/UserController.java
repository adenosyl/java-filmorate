package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final List<User> users = new ArrayList<>();
    private int nextId = 1;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        validateUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getId() == 0) {
            user.setId(nextId++);
        }
        users.add(user);
        log.info("Создан пользователь: {}", user);
        return ResponseEntity.ok(user);
    }

    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        validateUser(user);
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == user.getId()) {
                if (user.getName() == null || user.getName().isBlank()) {
                    user.setName(user.getLogin());
                }
                users.set(i, user);
                log.info("Обновлен пользователь: {}", user);
                return ResponseEntity.ok(user);
            }
        }
        log.error("Пользователь с id = {} не найден", user.getId());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Пользователь не найден");
        return ResponseEntity.status(404).body(error);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(users);
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Ошибка валидации: Некорректный email");
            throw new ValidationException("Некорректный email");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Ошибка валидации: Логин не может быть пустым и содержать пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Ошибка валидации: Дата рождения не может быть в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}