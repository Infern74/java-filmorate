package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.storage.dao.FeedDao;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedDao feedDao;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    public List<FeedEvent> getUserFeed(int userId) {
        userStorage.getById(userId);
        return feedDao.getEventsByUserId(userId);
    }

}
