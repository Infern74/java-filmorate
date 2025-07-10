package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.storage.dao.FeedDao;

@Component
@RequiredArgsConstructor
public class EventLogger {
    private final FeedDao feedDao;

    public void log(int userId, EventType type, OperationType operation, int entityId) {
        FeedEvent event = FeedEvent.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(type)
                .operationType(operation)
                .entityId(entityId)
                .build();
        feedDao.addEvent(event);
    }
}
