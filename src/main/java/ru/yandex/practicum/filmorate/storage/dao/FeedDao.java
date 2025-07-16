package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.OperationType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class FeedDao {

    private final JdbcTemplate jdbcTemplate;

    public FeedDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addEvent(FeedEvent event) {
        String sql = "INSERT INTO feed_events (timestamp,user_id,event_type,operation,entity_id) VALUES (?,?,?,?,?)";
        jdbcTemplate.update(sql, event.getTimestamp(), event.getUserId(), event.getEventType().name(), event.getOperationType().name(), event.getEntityId());
    }

    public List<FeedEvent> getEventsByUserId(int userId) {
        String sql = "SELECT * FROM feed_events WHERE user_id=? ORDER BY timestamp ASC";
        return jdbcTemplate.query(sql, this::mapRowToFeedEvent, userId);
    }

    private FeedEvent mapRowToFeedEvent(ResultSet rs, int rowNum) throws SQLException {
        return FeedEvent.builder()
                .eventId(rs.getInt("event_id"))
                .timestamp(rs.getLong("timestamp"))
                .userId(rs.getInt("user_id"))
                .eventType(EventType.valueOf(rs.getString("event_type")))
                .operationType(OperationType.valueOf(rs.getString("operation")))
                .entityId(rs.getInt("entity_id"))
                .build();
    }

}
