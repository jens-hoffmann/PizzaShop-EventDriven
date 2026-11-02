# Event Catalog

All integration events use a JSON envelope and JSON Schema validation. Schemas live in `shared/contracts/src/main/resources/schemas/event-envelope.schema.json`.

## Envelope

```json
{
  "eventId": "8f1c8f54-5a4f-4f4a-9a8f-0f6b8c2d7b2e",
  "eventType": "order.placed",
  "eventVersion": 1,
  "occurredAt": "2026-11-01T12:00:00Z",
  "producer": "order-service",
  "correlationId": "0d7d5b1e-8e75-4c1f-9b7e-4b0f3c7e6f1a",
  "causationId": null,
  "aggregateId": "3a6f6f5e-1c2b-4c1d-8d5f-2b7a9e0c4d31",
  "payload": {}
}
```

| Field | Meaning |
|---|---|
| `eventId` | Unique event ID, used by the inbox for deduplication |
| `eventType` | Stable event name, for example `order.placed` |
| `eventVersion` | Payload version; additive changes keep the version, breaking changes introduce a new event type or topic |
| `occurredAt` | When the domain event happened |
| `producer` | Producing service |
| `correlationId` | Request or event chain correlation ID |
| `causationId` | ID of the event that caused this event, if any |
| `aggregateId` | Aggregate/order ID; also used as the Kafka key |
| `payload` | Event-specific payload |

## Topics

| Topic | Key | Partitions | Producer | Consumers |
|---|---|---|---|---|
| `catalog.events.v1` | `pizzaId` | 1 | catalog-service | none in v1 |
| `order.events.v1` | `orderId` | 3 | order-service | payment-service, kitchen-service, notification-service |
| `payment.events.v1` | `orderId` | 3 | payment-service | order-service |
| `kitchen.events.v1` | `orderId` | 3 | kitchen-service | order-service |
| `<topic>.DLT` | original key | 1 | error handler | manual inspection/replay |

Local replication factor is 1. Production should use at least 3 brokers and replication factor 3, with `min.insync.replicas=2`.

## Event types

### Catalog

| Event | Payload |
|---|---|
| `catalog.pizza-created` | `PizzaSnapshot` |
| `catalog.pizza-updated` | `PizzaSnapshot` |
| `catalog.pizza-availability-changed` | `pizzaId`, `available`, `updatedAt` |

### Order

| Event | Payload |
|---|---|
| `order.placed` | `orderId`, `customerId`, items, `total`, `currency`, `deliveryAddress`, `placedAt` |
| `order.paid` | `orderId`, `paymentId`, `paidAt` |
| `order.payment-failed` | `orderId`, `reason`, `failedAt` |
| `order.cancelled` | `orderId`, `reason`, `cancelledAt`, `wasPaid` |
| `order.completed` | `orderId`, `completedAt` |
| `order.status-changed` | `orderId`, `customerId`, `previousStatus`, `newStatus`, `reason`, `occurredAt` |

`previousStatus` is `null` for the initial `PLACED` transition.

### Payment

| Event | Payload |
|---|---|
| `payment.succeeded` | `paymentId`, `orderId`, `amount`, `currency`, `paidAt` |
| `payment.failed` | `paymentId`, `orderId`, `reason`, `failedAt` |
| `payment.refunded` | `paymentId`, `orderId`, `amount`, `refundedAt`, `reason` |

### Kitchen

| Event | Payload |
|---|---|
| `kitchen.ticket-accepted` | `ticketId`, `orderId`, `acceptedAt` |
| `kitchen.preparation-started` | `ticketId`, `orderId`, `startedAt` |
| `kitchen.order-ready` | `ticketId`, `orderId`, `readyAt` |
| `kitchen.ticket-cancelled` | `ticketId`, `orderId`, `reason`, `cancelledAt` |

## Contract rules

- The JSON Schema is the canonical contract. Java records mirror the payloads for producers and consumers.
- Validation runs in `shared/contracts` unit tests and on every consumed event. Invalid events are non-retryable and go to the DLT.
- Within a topic version, changes must be additive. Consumers ignore unknown fields.
- Breaking payload changes require a new event type and/or a new topic version.
- `eventId` is globally unique and is the deduplication key.
- The Kafka key is the aggregate ID (`orderId` or `pizzaId`). Per-key ordering is guaranteed within a partition.
- Events are immutable; corrections are new events, not updates to old events.

## Failure handling

- Transient consumer failures are retried three times with a one-second backoff.
- `InvalidEventException` and schema violations are routed to the DLT immediately.
- DLT messages retain the original payload and key for inspection with Kafka UI or `kafka-console-consumer`.
- The outbox publisher retries failed sends with exponential backoff and records `attempts`, `next_attempt_at`, and `last_error` in `outbox_message`.
