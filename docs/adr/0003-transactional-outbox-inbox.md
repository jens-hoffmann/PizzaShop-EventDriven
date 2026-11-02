# ADR 0003: Transactional outbox and idempotent inbox

## Status

Accepted.

## Context

Publishing to Kafka directly after a database commit can lose events if the process crashes between the two operations. Publishing inside the transaction does not make the database and Kafka atomic. Kafka delivery is at least once, so consumers must tolerate duplicates.

## Decision

- Each producing service writes an `outbox_message` row in the same transaction as the domain change.
- A scheduled publisher reads due rows with `FOR UPDATE SKIP LOCKED`, sends synchronously, marks them published, and retries failures with exponential backoff.
- Each consumer inserts an event ID into `processed_event` in the same transaction as its side effects. A duplicate insert means the event is skipped.
- Producer idempotence and `acks=all` are enabled.
- No XA or Kafka transactions are used.

## Consequences

- Domain changes and event publication are eventually consistent but never silently lost after a commit.
- Consumers are effectively once for side effects.
- Outbox rows require cleanup and monitoring.
- A polling publisher adds a small latency (default 500 ms).
