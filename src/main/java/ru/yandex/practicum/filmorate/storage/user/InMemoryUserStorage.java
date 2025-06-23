package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int idCounter = 1;

    @Override
    public User create(User user) {
        user.setId(idCounter++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new UserNotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User delete(int id) {
        if (!users.containsKey(id)) {
            throw new UserNotFoundException("Пользователь с id=" + id + " не найден");
        }
        return users.remove(id);
    }

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public User getById(int id) {
        if (!users.containsKey(id)) {
            throw new UserNotFoundException("Пользователь с id=" + id + " не найден");
        }
        return users.get(id);
    }
}