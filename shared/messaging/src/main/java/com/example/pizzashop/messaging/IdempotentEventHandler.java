package com.example.pizzashop.messaging;

import com.example.pizzashop.contracts.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.function.Consumer;

public class IdempotentEventHandler {

    private static final Logger log = LoggerFactory.getLogger(IdempotentEventHandler.class);

    private final ProcessedEventRepository repository;
    private final ObjectMapper objectMapper;

    public IdempotentEventHandler(ProcessedEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public <T> void handle(
            EventEnvelope<JsonNode> rawEnvelope,
            String consumer,
            Class<T> payloadType,
            Consumer<EventEnvelope<T>> handler
    ) {
        int inserted = repository.insertIfAbsent(rawEnvelope.eventId(), consumer, Instant.now());
        if (inserted == 0) {
            log.debug("Skipping duplicate event eventId={} consumer={}", rawEnvelope.eventId(), consumer);
            return;
        }

        T payload;
        try {
            payload = objectMapper.treeToValue(rawEnvelope.payload(), payloadType);
        } catch (Exception exception) {
            throw new InvalidEventException(
                    "Unable to map payload for event " + rawEnvelope.eventType(),
                    exception
            );
        }

        EventEnvelope<T> envelope = new EventEnvelope<>(
                rawEnvelope.eventId(),
                rawEnvelope.eventType(),
                rawEnvelope.eventVersion(),
                rawEnvelope.occurredAt(),
                rawEnvelope.producer(),
                rawEnvelope.correlationId(),
                rawEnvelope.causationId(),
                rawEnvelope.aggregateId(),
                payload
        );
        handler.accept(envelope);
    }
}
