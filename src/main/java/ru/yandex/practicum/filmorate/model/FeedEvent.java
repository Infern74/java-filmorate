package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeedEvent {

    private int eventId;
    private long timestamp;
    private int userId;
    private EventType eventType;
    @JsonProperty("operation")
    private OperationType operationType;
    private int entityId;

}
