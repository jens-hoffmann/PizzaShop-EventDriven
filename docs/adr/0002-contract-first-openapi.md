# ADR 0002: Contract-first OpenAPI

## Status

Accepted.

## Context

The REST API must be documented and stable. The project should use OpenAPI tooling where reasonable. Code-first generation couples the contract to controller annotations; contract-first makes the specification the source of truth.

## Decision

Keep OpenAPI 3.1 specifications in `openapi/`. Each service generates Spring MVC interfaces and models with `openapi-generator-maven-plugin` 7.26.0 (`interfaceOnly`, `useSpringBoot3`, `useJakartaEe`, `openApiNullable=false`, `generateSupportingFiles=false`). REST adapters implement the generated interfaces and map generated models to domain objects.

Each service serves its specification as a static resource, and a Swagger UI container aggregates all specs from the `openapi/` directory.

## Consequences

- The API contract is reviewable independently of implementation.
- Generated code stays in adapter packages.
- Spec changes require regeneration and adapter updates.
- Generated models are deliberately not used in the domain or application layers.
