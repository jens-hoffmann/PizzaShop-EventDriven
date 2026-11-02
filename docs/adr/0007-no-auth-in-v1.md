# ADR 0007: No authentication in v1

## Status

Accepted for v1.

## Context

Authentication, authorization, and service-to-service identity would add significant scope to the first version. The project focuses on REST contracts, hexagonal architecture, and reliable event processing.

## Decision

Do not add authentication in v1. Public customer endpoints and `/api/v1/admin` endpoints are unsecured. Admin routes are namespaced so a security boundary is easy to add later. Actuator exposes only health, info, and metrics.

## Consequences

- Faster delivery of the event-driven core.
- The API must not be exposed to untrusted networks as-is.
- A follow-up should add an identity provider, token validation, role checks, and service-to-service credentials.
