package com.example.pizzashop.messaging;

import java.util.Objects;
import java.util.UUID;

public class ProcessedEventId implements java.io.Serializable {

    private UUID eventId;
    private String consumer;

    public ProcessedEventId() {
    }

    public ProcessedEventId(UUID eventId, String consumer) {
        this.eventId = eventId;
        this.consumer = consumer;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getConsumer() {
        return consumer;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ProcessedEventId that)) {
            return false;
        }
        return Objects.equals(eventId, that.eventId) && Objects.equals(consumer, that.consumer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, consumer);
    }
}
