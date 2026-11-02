# ADR 0005: Synchronous catalog pricing at checkout

## Status

Accepted.

## Context

Order creation needs authoritative pizza names, availability, and prices. A catalog read model built from Kafka events would be eventually consistent and could accept an order at a stale or unavailable price.

## Decision

`order-service` calls `catalog-service` synchronously when placing an order, using a short connect/read timeout. It fetches all requested pizzas in one call, rejects unavailable or unknown pizzas with HTTP 422, and snapshots names and prices into the order.

## Consequences

- Prices are authoritative at the moment of checkout.
- Checkout availability depends on the catalog service; failures return HTTP 503.
- Orders retain historical prices even if the menu changes.
- A future version can add caching or a read model if the latency becomes a problem.
