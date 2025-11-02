package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserDbStorageFriendTest {

    @Autowired
    private UserDbStorage userDbStorage;

    @Test
    void testAddAndRemoveFriend_OneWay() {
        User user1 = new User();
        user1.setEmail("test1@example.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        user1 = userDbStorage.add(user1);

        User user2 = new User();
        user2.setEmail("test2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1992, 2, 2));
        user2 = userDbStorage.add(user2);

        // Добавляем user2 в друзья user1
        userDbStorage.addFriend(user1.getId(), user2.getId());

        Set<Integer> user1Friends = userDbStorage.findById(user1.getId()).getFriends();
        Set<Integer> user2Friends = userDbStorage.findById(user2.getId()).getFriends();

        assertThat(user1Friends).contains(user2.getId());
        assertThat(user2Friends).doesNotContain(user1.getId());

        // Удаляем дружбу
        userDbStorage.removeFriend(user1.getId(), user2.getId());

        user1Friends = userDbStorage.findById(user1.getId()).getFriends();
        assertThat(user1Friends).doesNotContain(user2.getId());

        // Чистим
        userDbStorage.delete(user1.getId());
        userDbStorage.delete(user2.getId());
    }
}
