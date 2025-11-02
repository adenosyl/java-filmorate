package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTests {

    private UserController userController;

    @BeforeEach
    void setUp() {
        // создаём простую in-memory инфраструктуру для теста
        var userStorage = new InMemoryUserStorage();
        var userService = new UserService(userStorage);
        userController = new UserController(userService);
    }

    @Test
    void createUser_ValidData_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.createUser(user);

        assertNotNull(created.getId());
        assertEquals("test@example.com", created.getEmail());
        assertEquals("testlogin", created.getLogin());
        assertEquals("Test User", created.getName());
    }

    @Test
    void createUser_EmptyEmail_ThrowsException() {
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        user.setName("User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> userController.createUser(user)
        );
        assertEquals("Некорректный email", ex.getMessage());
    }

    @Test
    void createUser_InvalidEmailFormat_ThrowsException() {
        User user = new User();
        user.setEmail("invalidEmailFormat");
        user.setLogin("login");
        user.setName("User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> userController.createUser(user)
        );
        assertEquals("Некорректный email", ex.getMessage());
    }

    @Test
    void createUser_LoginContainsSpaces_ThrowsException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("bad login");
        user.setName("User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> userController.createUser(user)
        );
        assertEquals("Логин не может быть пустым и содержать пробелы", ex.getMessage());
    }

    @Test
    void createUser_EmptyLogin_ThrowsException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("");
        user.setName("User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> userController.createUser(user)
        );
        assertEquals("Логин не может быть пустым и содержать пробелы", ex.getMessage());
    }

    @Test
    void createUser_BirthdayInFuture_ThrowsException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("User");
        user.setBirthday(LocalDate.now().plusDays(1)); // будущее

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> userController.createUser(user)
        );
        assertEquals("Дата рождения не может быть в будущем", ex.getMessage());
    }

    @Test
    void createUser_EmptyName_ReplacesWithLogin() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.createUser(user);

        assertEquals("testlogin", created.getName()); // имя подставляется логином
    }
}