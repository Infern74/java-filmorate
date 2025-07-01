package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.FriendshipDao;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
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
        getUserOrThrow(userId);
        getUserOrThrow(friendId);
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
        return friendshipDao.getFriends(userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        getUserOrThrow(userId);
        getUserOrThrow(otherId);

        List<User> userFriends = friendshipDao.getFriends(userId);
        List<User> otherFriends = friendshipDao.getFriends(otherId);

        Set<Integer> otherFriendIds = otherFriends.stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        return userFriends.stream()
                .filter(user -> otherFriendIds.contains(user.getId()))
                .collect(Collectors.toList());
    }

    private User getUserOrThrow(int id) {
        return userStorage.getById(id);
    }
}