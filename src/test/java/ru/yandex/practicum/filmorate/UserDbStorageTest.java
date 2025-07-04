package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .login("testlogin")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void createUser_ShouldReturnUserWithId() {
        User createdUser = userStorage.create(testUser);

        assertThat(createdUser.getId()).isPositive();
        assertThat(createdUser.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(createdUser.getLogin()).isEqualTo(testUser.getLogin());
    }

    @Test
    void updateUser_ShouldUpdateFields() {
        User createdUser = userStorage.create(testUser);
        User updatedUser = createdUser.toBuilder()
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        User result = userStorage.update(updatedUser);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void getById_ShouldReturnCorrectUser() {
        User createdUser = userStorage.create(testUser);
        User foundUser = userStorage.getById(createdUser.getId());

        assertThat(foundUser).isEqualTo(createdUser);
    }

    @Test
    void getById_ShouldThrowExceptionForNonExistingUser() {
        assertThatThrownBy(() -> userStorage.getById(999))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Пользователь с id=999 не найден");
    }

    @Test
    void getAll_ShouldReturnAllUsers() {
        User user1 = userStorage.create(testUser);
        User user2 = userStorage.create(testUser.toBuilder().email("another@example.com").login("anotherlogin").build());

        Collection<User> users = userStorage.getAll();

        assertThat(users).hasSize(2);
        assertThat(users).containsExactlyInAnyOrder(user1, user2);
    }

    @Test
    void deleteUser_ShouldRemoveUser() {
        User createdUser = userStorage.create(testUser);
        userStorage.delete(createdUser.getId());

        assertThatThrownBy(() -> userStorage.getById(createdUser.getId()))
                .isInstanceOf(UserNotFoundException.class);
    }
}