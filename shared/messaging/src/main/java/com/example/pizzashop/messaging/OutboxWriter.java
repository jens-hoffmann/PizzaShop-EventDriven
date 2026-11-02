package com.example.pizzashop.messaging;

import com.example.pizzashop.contracts.EventEnvelope;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

public class OutboxWriter {

    private final OutboxRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxWriter(OutboxRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public void append(EventEnvelope<?> envelope) {
        try {
            String payload = objectMapper.writeValueAsString(envelope);
            repository.save(new OutboxMessage(
                    envelope.eventId(),
                    envelope.aggregateId(),
                    envelope.eventType(),
                    envelope.eventVersion(),
                    payload,
                    envelope.occurredAt(),
                    Instant.now()
            ));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to append event to outbox: " + envelope.eventType(), exception);
        }
    }
}
