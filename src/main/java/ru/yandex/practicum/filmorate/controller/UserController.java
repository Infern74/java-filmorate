package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Запрос на создание пользователя: {}", user);
        validateUser(user);
        setUserNameIfEmpty(user);
        User createdUser = userService.create(user);
        log.info("Создан новый пользователь: {}", createdUser);
        return createdUser;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        log.info("Запрос на обновление пользователя: {}", user);
        validateUser(user);
        setUserNameIfEmpty(user);
        User updatedUser = userService.update(user);
        log.info("Обновлен пользователь: {}", updatedUser);
        return updatedUser;
    }

    @GetMapping
    public Collection<User> getAll() {
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable int id) {
        return userService.getById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(
            @PathVariable int id,
            @PathVariable int friendId) {
        log.info("Запрос на добавление в друзья: {} -> {}", id, friendId);
        userService.addFriend(id, friendId);
        log.info("Пользователь {} отправил заявку в друзья пользователю {}", id, friendId);
    }

    @PutMapping("/{id}/friends/{friendId}/confirm")
    public void confirmFriend(
            @PathVariable int id,
            @PathVariable int friendId) {
        log.info("Подтверждение дружбы: {} подтверждает {}", id, friendId);
        userService.confirmFriend(id, friendId);
        log.info("Пользователь {} подтвердил дружбу с пользователем {}", id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFriend(
            @PathVariable int id,
            @PathVariable int friendId) {
        userService.removeFriend(id, friendId);
        log.info("Пользователь {} удалил из друзей пользователя {}", id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable int id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(
            @PathVariable int id,
            @PathVariable int otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    private void setUserNameIfEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Некорректный email: {}", user.getEmail());
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Некорректный логин: {}", user.getLogin());
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() == null) {
            log.error("Дата рождения не указана");
            throw new ValidationException("Дата рождения обязательна");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Некорректная дата рождения: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}