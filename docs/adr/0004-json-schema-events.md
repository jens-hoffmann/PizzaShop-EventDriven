# ADR 0004: JSON Schema event contracts without a registry

## Status

Accepted.

## Context

The project needs explicit event contracts and validation without adding a schema registry to the local stack. A registry provides stronger governance but adds infrastructure and serialization coupling.

## Decision

Define a single JSON Schema (draft 2020-12) for the event envelope and all payloads in `shared/contracts`. Use Java records for typed payloads and validate envelopes with NetworkNT JSON Schema Validator in tests and on consumption. Use string serialization with an explicit envelope rather than type headers.

Version events with `eventVersion` and version topics with `.v1`. Additive changes are allowed within a version; breaking changes require a new event type or topic.

## Consequences

- No extra infrastructure and no serializer lock-in.
- The schema is checked into the repository and easy to review.
- Schema evolution is a convention rather than registry-enforced.
- A future migration to Avro/Protobuf plus a registry is possible without changing domain code.
