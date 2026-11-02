package com.example.pizzashop.contracts;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class EventSchemaValidatorTest {

    private final JsonMapper mapper = JsonMapper.builder().build();
    private final EventSchemaValidator validator = new EventSchemaValidator();

    @Test
    void acceptsAValidOrderPlacedEnvelope() {
        String json = """
                {
                  "eventId": "11111111-1111-1111-1111-111111111111",
                  "eventType": "order.placed",
                  "eventVersion": 1,
                  "occurredAt": "2026-11-01T12:00:00Z",
                  "producer": "order-service",
                  "correlationId": "22222222-2222-2222-2222-222222222222",
                  "causationId": null,
                  "aggregateId": "33333333-3333-3333-3333-333333333333",
                  "payload": {
                    "orderId": "33333333-3333-3333-3333-333333333333",
                    "customerId": "customer-1",
                    "items": [
                      {
                        "pizzaId": "44444444-4444-4444-4444-444444444444",
                        "name": "Margherita",
                        "size": "LARGE",
                        "unitPrice": 10.50,
                        "quantity": 2,
                        "lineTotal": 21.00
                      }
                    ],
                    "total": 21.00,
                    "currency": "EUR",
                    "deliveryAddress": {
                      "street": "Main Street 1",
                      "city": "Pizza Town",
                      "postalCode": "12345"
                    },
                    "placedAt": "2026-11-01T12:00:00Z"
                  }
                }
                """;

        assertThat(validator.validate(mapper.readTree(json))).isEmpty();
    }

    @Test
    void rejectsAPayloadThatDoesNotMatchTheEventType() {
        String json = """
                {
                  "eventId": "11111111-1111-1111-1111-111111111111",
                  "eventType": "order.placed",
                  "eventVersion": 1,
                  "occurredAt": "2026-11-01T12:00:00Z",
                  "producer": "order-service",
                  "correlationId": "22222222-2222-2222-2222-222222222222",
                  "aggregateId": "33333333-3333-3333-3333-333333333333",
                  "payload": {
                    "orderId": "33333333-3333-3333-3333-333333333333"
                  }
                }
                """;

        assertThat(validator.validate(mapper.readTree(json))).isNotEmpty();
    }

    @Test
    void rejectsAnUnknownEventType() {
        String json = """
                {
                  "eventId": "11111111-1111-1111-1111-111111111111",
                  "eventType": "order.unknown",
                  "eventVersion": 1,
                  "occurredAt": "2026-11-01T12:00:00Z",
                  "producer": "order-service",
                  "correlationId": "22222222-2222-2222-2222-222222222222",
                  "aggregateId": "33333333-3333-3333-3333-333333333333",
                  "payload": {}
                }
                """;

        assertThat(validator.validate(mapper.readTree(json))).isNotEmpty();
    }

    @Test
    void acceptsInitialStatusChangeWithoutAPreviousStatus() {
        String json = """
                {
                  "eventId": "11111111-1111-1111-1111-111111111111",
                  "eventType": "order.status-changed",
                  "eventVersion": 1,
                  "occurredAt": "2026-11-01T12:00:00Z",
                  "producer": "order-service",
                  "correlationId": "22222222-2222-2222-2222-222222222222",
                  "causationId": null,
                  "aggregateId": "33333333-3333-3333-3333-333333333333",
                  "payload": {
                    "orderId": "33333333-3333-3333-3333-333333333333",
                    "customerId": "customer-1",
                    "previousStatus": null,
                    "newStatus": "PLACED",
                    "reason": "Order placed",
                    "occurredAt": "2026-11-01T12:00:00Z"
                  }
                }
                """;

        assertThat(validator.validate(mapper.readTree(json))).isEmpty();
    }

    @Test
    void acceptsAPizzaWithoutADescription() {
        String json = """
                {
                  "eventId": "11111111-1111-1111-1111-111111111111",
                  "eventType": "catalog.pizza-created",
                  "eventVersion": 1,
                  "occurredAt": "2026-11-01T12:00:00Z",
                  "producer": "catalog-service",
                  "correlationId": "22222222-2222-2222-2222-222222222222",
                  "causationId": null,
                  "aggregateId": "33333333-3333-3333-3333-333333333333",
                  "payload": {
                    "pizzaId": "33333333-3333-3333-3333-333333333333",
                    "name": "Margherita",
                    "description": null,
                    "available": true,
                    "prices": {"SMALL": 7.50, "MEDIUM": 9.50, "LARGE": 11.50},
                    "updatedAt": "2026-11-01T12:00:00Z"
                  }
                }
                """;

        assertThat(validator.validate(mapper.readTree(json))).isEmpty();
    }
}
