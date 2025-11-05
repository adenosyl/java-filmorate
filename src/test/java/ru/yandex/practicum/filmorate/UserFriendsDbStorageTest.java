package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserFriendsDbStorageTest {

    @Autowired
    private UserDbStorage userDbStorage;

    @Test
    void testAddAndRemoveFriend() {
        // Создаем пользователей
        User user1 = new User();
        user1.setEmail("user1@friend.test");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        user1 = userDbStorage.add(user1);

        User user2 = new User();
        user2.setEmail("user2@friend.test");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1992, 2, 2));
        user2 = userDbStorage.add(user2);

        // Проверяем, что у user1 нет друзей
        Set<Integer> user1Friends = userDbStorage.findById(user1.getId()).getFriends();
        assertThat(user1Friends).isEmpty();

        // Добавляем user2 в друзья user1
        userDbStorage.addFriend(user1.getId(), user2.getId());

        // Проверяем, что user2 появился в друзьях user1
        user1Friends = userDbStorage.findById(user1.getId()).getFriends();
        assertThat(user1Friends).contains(user2.getId());

        // Проверяем, что дружба односторонняя — user2 не имеет user1 в друзьях
        Set<Integer> user2Friends = userDbStorage.findById(user2.getId()).getFriends();
        assertThat(user2Friends).doesNotContain(user1.getId());

        // Удаляем дружбу
        userDbStorage.removeFriend(user1.getId(), user2.getId());

        // Проверяем, что друзья у user1 пусты
        user1Friends = userDbStorage.findById(user1.getId()).getFriends();
        assertThat(user1Friends).doesNotContain(user2.getId());

        // Очистка
        userDbStorage.delete(user1.getId());
        userDbStorage.delete(user2.getId());
    }
}
