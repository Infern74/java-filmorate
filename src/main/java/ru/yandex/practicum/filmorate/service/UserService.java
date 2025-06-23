package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
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
        log.debug("Запрос на дружбу: {} -> {}", userId, friendId);
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        user.getFriends().put(friendId, FriendshipStatus.PENDING);
        friend.getFriends().put(userId, FriendshipStatus.PENDING);
    }

    public void confirmFriend(int userId, int friendId) {
        log.debug("Подтверждение дружбы: {} подтверждает {}", userId, friendId);
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        if (!user.getFriends().containsKey(friendId) ||
                !friend.getFriends().containsKey(userId)) {
            throw new IllegalArgumentException("Запрос на дружбу не найден");
        }

        user.getFriends().put(friendId, FriendshipStatus.CONFIRMED);
        friend.getFriends().put(userId, FriendshipStatus.CONFIRMED);
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

        Set<Integer> friendIds = new HashSet<>(user.getFriends().keySet());

        for (Integer friendId : friendIds) {
            User friend = userStorage.getById(friendId);
            friend.getFriends().remove(userId);
        }

        userStorage.delete(userId);
    }

    public List<User> getFriends(int userId) {
        User user = getUserOrThrow(userId);
        return user.getFriends().keySet().stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        User user = getUserOrThrow(userId);
        User other = getUserOrThrow(otherId);

        return user.getFriends().keySet().stream()
                .filter(id -> other.getFriends().containsKey(id))
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    private User getUserOrThrow(int id) {
        return userStorage.getById(id);
    }
}