package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.FriendshipDao;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserStorage userStorage;
    private final FriendshipDao friendshipDao;

    @Autowired
    public UserService(
            @Qualifier("userDbStorage") UserStorage userStorage,
            FriendshipDao friendshipDao) {
        this.userStorage = userStorage;
        this.friendshipDao = friendshipDao;
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
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);
        friendshipDao.addFriend(userId, friendId);
    }

    public void confirmFriend(int userId, int friendId) {
        getUserOrThrow(userId);
        getUserOrThrow(friendId);
        friendshipDao.confirmFriend(userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        getUserOrThrow(userId);
        getUserOrThrow(friendId);
        friendshipDao.removeFriend(userId, friendId);
    }

    public List<User> getFriends(int userId) {
        getUserOrThrow(userId);
        List<Integer> friendIds = friendshipDao.getFriends(userId);
        return friendIds.stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        User user = getUserOrThrow(userId);
        User other = getUserOrThrow(otherId);

        List<Integer> userFriends = friendshipDao.getFriends(userId);
        List<Integer> otherFriends = friendshipDao.getFriends(otherId);

        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    private User getUserOrThrow(int id) {
        return userStorage.getById(id);
    }
}