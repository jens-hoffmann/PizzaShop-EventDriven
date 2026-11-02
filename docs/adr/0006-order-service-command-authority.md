# ADR 0006: Order service as command and state authority

## Status

Accepted.

## Context

Payment and kitchen could react to each other's events directly. That creates cross-topic races: the kitchen could accept an order before the order service has processed the payment result.

## Decision

`order-service` is the authority for order state. Payment and kitchen consume commands from `order.events.v1`; their result events are consumed by `order-service`, which applies transitions and publishes `order.status-changed`.

All commands for one order use the order ID as the Kafka key, so payment and kitchen observe them in order. Kitchen preparation starts only after `order.paid` is published by the order service.

## Consequences

- No cross-topic race between payment and kitchen for the same order.
- `order-service` is on the critical path for status changes.
- Notification has a single, simple stream to consume.
- The order state machine remains the single source of truth.
