package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTests {

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @Test
    void createUser_ValidData_Success() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setLogin("userlogin");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 5, 5));

        var response = userController.createUser(user);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("userlogin", response.getBody().getName()); // если name пустой
    }

    @Test
    void createUser_EmptyEmail_ThrowsException() {
        User user = new User();
        user.setId(1);
        user.setEmail("");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        var ex = assertThrows(ValidationException.class, () -> userController.createUser(user));
        assertEquals("Некорректный email", ex.getMessage());
    }

    @Test
    void createUser_InvalidEmail_ThrowsException() {
        User user = new User();
        user.setId(1);
        user.setEmail("invalidemail");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        var ex = assertThrows(ValidationException.class, () -> userController.createUser(user));
        assertEquals("Некорректный email", ex.getMessage());
    }

    @Test
    void createUser_LoginWithSpaces_ThrowsException() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setLogin("user login");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        var ex = assertThrows(ValidationException.class, () -> userController.createUser(user));
        assertEquals("Логин не может быть пустым и содержать пробелы", ex.getMessage());
    }

    @Test
    void createUser_FutureBirthday_ThrowsException() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setLogin("userlogin");
        user.setBirthday(LocalDate.now().plusDays(1));

        var ex = assertThrows(ValidationException.class, () -> userController.createUser(user));
        assertEquals("Дата рождения не может быть в будущем", ex.getMessage());
    }
}