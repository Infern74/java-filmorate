package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

public interface UserStorage {

    User create(User user);

    User update(User user);

    User delete(int id);

    void deleteUser(int id);

    Collection<User> getAll();

    User getById(int id);

    List<User> getUsersByIds(List<Integer> ids);
}