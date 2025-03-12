package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController controller;
    private User validUser;

    @BeforeEach
    void setUp() {
        controller = new UserController();
        validUser = new User();
        validUser.setEmail("valid@email.com");
        validUser.setLogin("validLogin");
        validUser.setBirthday(LocalDate.of(2000, 1, 1));
    }

    @Test
    void shouldThrowExceptionWhenEmailInvalid() {
        validUser.setEmail("invalid-email");
        assertThrows(ValidationException.class, () -> controller.create(validUser));
    }

    @Test
    void shouldThrowExceptionWhenLoginEmpty() {
        validUser.setLogin("");
        assertThrows(ValidationException.class, () -> controller.create(validUser));
    }

    @Test
    void shouldThrowExceptionWhenLoginHasSpaces() {
        validUser.setLogin("login with spaces");
        assertThrows(ValidationException.class, () -> controller.create(validUser));
    }

    @Test
    void shouldThrowExceptionWhenBirthdayInFuture() {
        validUser.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> controller.create(validUser));
    }

    @Test
    void shouldUseLoginWhenNameIsEmpty() {
        validUser.setName("");
        User created = controller.create(validUser);
        assertEquals(validUser.getLogin(), created.getName());
    }

    @Test
    void shouldThrowExceptionWhenUpdateWithInvalidEmail() {
        User created = controller.create(validUser);
        created.setEmail("invalid-email");
        assertThrows(ValidationException.class, () -> controller.update(created));
    }

    @Test
    void shouldThrowExceptionWhenUpdateWithEmptyLogin() {
        User created = controller.create(validUser);
        created.setLogin("");
        assertThrows(ValidationException.class, () -> controller.update(created));
    }

    @Test
    void shouldThrowExceptionWhenUpdateWithSpacesInLogin() {
        User created = controller.create(validUser);
        created.setLogin("login with spaces");
        assertThrows(ValidationException.class, () -> controller.update(created));
    }

    @Test
    void shouldThrowExceptionWhenUpdateWithFutureBirthday() {
        User created = controller.create(validUser);
        created.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> controller.update(created));
    }

    @Test
    void shouldUpdateNameWhenEmpty() {
        User created = controller.create(validUser);
        created.setName("");
        User updated = controller.update(created);
        assertEquals(created.getLogin(), updated.getName());
    }

    @Test
    void shouldUpdateSuccessfullyWithValidData() {
        User created = controller.create(validUser);
        created.setName("New Name");
        created.setEmail("new@email.com");
        User updated = controller.update(created);

        assertAll(
                () -> assertEquals("New Name", updated.getName()),
                () -> assertEquals("new@email.com", updated.getEmail())
        );
    }

    @Test
    void shouldReturnAllUsers() {
        User user1 = new User();
        user1.setEmail("user1@mail.com");
        user1.setLogin("login1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@mail.com");
        user2.setLogin("login2");
        user2.setBirthday(LocalDate.of(1995, 1, 1));

        controller.create(user1);
        controller.create(user2);

        Collection<User> allUsers = controller.getAll();

        assertAll(
                () -> assertEquals(2, allUsers.size()),
                () -> assertTrue(allUsers.contains(user1)),
                () -> assertTrue(allUsers.contains(user2))
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoUsers() {
        Collection<User> allUsers = controller.getAll();
        assertTrue(allUsers.isEmpty());
    }
}