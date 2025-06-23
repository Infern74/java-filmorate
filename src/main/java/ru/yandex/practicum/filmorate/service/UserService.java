package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public User getById(int id) {
        return userStorage.getById(id);
    }

    public void addFriend(int userId, int friendId) {
        log.debug("Добавление друга: {} -> {}", userId, friendId);
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    public void removeFriend(int userId, int friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        log.info("Удалена дружба между {} и {}", userId, friendId);
    }

    public void removeUser(int userId) {
        User user = getUserOrThrow(userId);

        for (Integer friendId : user.getFriends()) {
            User friend = userStorage.getById(friendId);
            friend.getFriends().remove(userId);
        }

        userStorage.delete(userId);
    }

    public List<User> getFriends(int userId) {
        User user = getUserOrThrow(userId);
        List<User> friends = new ArrayList<>();
        for (Integer friendId : user.getFriends()) {
            friends.add(userStorage.getById(friendId));
        }
        return friends;
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        User user = getUserOrThrow(userId);
        User other = getUserOrThrow(otherId);

        return user.getFriends().stream()
                .filter(id -> other.getFriends().contains(id))
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    private User getUserOrThrow(int id) {
        return userStorage.getById(id);
    }
}