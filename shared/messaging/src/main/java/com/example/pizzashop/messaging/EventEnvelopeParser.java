package com.example.pizzashop.messaging;

import com.example.pizzashop.contracts.EventEnvelope;
import com.example.pizzashop.contracts.EventSchemaValidator;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class EventEnvelopeParser {

    private final ObjectMapper objectMapper;
    private final EventSchemaValidator schemaValidator;

    public EventEnvelopeParser(ObjectMapper objectMapper, EventSchemaValidator schemaValidator) {
        this.objectMapper = objectMapper;
        this.schemaValidator = schemaValidator;
    }

    public EventEnvelope<JsonNode> parse(String rawEvent) {
        try {
            JsonNode tree = objectMapper.readTree(rawEvent);
            List<String> errors = schemaValidator.validate(tree);
            if (!errors.isEmpty()) {
                throw new InvalidEventException("Invalid event envelope: " + String.join("; ", errors));
            }
            JsonNode causationNode = tree.path("causationId");
            UUID causationId = causationNode.isMissingNode() || causationNode.isNull()
                    ? null
                    : UUID.fromString(causationNode.asText());
            return new EventEnvelope<>(
                    UUID.fromString(tree.path("eventId").asText()),
                    tree.path("eventType").asText(),
                    tree.path("eventVersion").asInt(),
                    Instant.parse(tree.path("occurredAt").asText()),
                    tree.path("producer").asText(),
                    UUID.fromString(tree.path("correlationId").asText()),
                    causationId,
                    tree.path("aggregateId").asText(),
                    tree.path("payload")
            );
        } catch (InvalidEventException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidEventException("Unable to parse event envelope", exception);
        }
    }
}
