package com.example.pizzashop.contracts;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        String producer,
        UUID correlationId,
        UUID causationId,
        String aggregateId,
        T payload
) {

    public static <T> EventEnvelope<T> create(
            String eventType,
            String producer,
            UUID correlationId,
            UUID causationId,
            String aggregateId,
            T payload
    ) {
        return new EventEnvelope<>(
                UUID.randomUUID(),
                eventType,
                1,
                Instant.now(),
                producer,
                correlationId,
                causationId,
                aggregateId,
                payload
        );
    }
}
