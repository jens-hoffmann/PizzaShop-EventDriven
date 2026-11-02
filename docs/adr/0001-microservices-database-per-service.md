# ADR 0001: Microservices with a database per service

## Status

Accepted.

## Context

The shop has distinct responsibilities: menu, ordering, payment, kitchen, and notifications. A single deployable would be simpler, but the goal is to demonstrate event-driven integration, independent data ownership, and failure isolation.

## Decision

Use five Spring Boot services. Each service owns a private logical PostgreSQL database and communicates with other services through Kafka events or explicit REST calls. No service reads another service's tables.

Local development uses one PostgreSQL container with five databases to keep resource usage reasonable. Production should use separate database instances and credentials.

## Consequences

- Clear boundaries and independent evolution.
- Eventual consistency and operational overhead.
- Integration testing requires Kafka and multiple databases.
- Cross-service queries require read models or API calls rather than SQL joins.
