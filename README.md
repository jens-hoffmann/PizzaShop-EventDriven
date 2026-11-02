# Pizza Shop Event-Driven Backend

A backend for a pizza web shop built around event-driven order processing. The system is split into five Spring Boot services, uses PostgreSQL per service, Kafka for integration events, and follows a hexagonal ports-and-adapters architecture.

## Architecture at a glance

```
                     REST (browser, curl, Swagger UI)
                                    |
  +------------+       +------------+-------------+       +----------------+
  |  catalog   |<------|          order             |------>|  notification  |
  |  :8081     | price |          :8082             | status |  :8085         |
  +-----+------+ quote +------+------------+--------+ events +--------+-------+
        |                     |            |                         ^
        |             order.events.v1      |                         |
        |                     |            |                         |
        |          +----------v---+   +----v---------+               |
        |          |   payment    |   |   kitchen    |               |
        +--------->|   :8084      |   |   :8083      |               |
         REST      +------+-------+   +------+-------+               |
                          |                  |                       |
                   payment.events.v1  kitchen.events.v1 -------------+
                          |                  |
                          +--------+---------+
                                   |
                             order-service
```

| Service | Responsibility | Port |
|---|---|---|
| `catalog-service` | Menu catalog, sizes, prices, availability | 8081 |
| `order-service` | Order aggregate, state machine, idempotency, tracking | 8082 |
| `kitchen-service` | Simulated kitchen tickets and preparation timers | 8083 |
| `payment-service` | Simulated payment and refund processing | 8084 |
| `notification-service` | Simulated customer notifications and read model | 8085 |

Supporting infrastructure: PostgreSQL 18, Kafka 4.4 (KRaft), Kafka UI on `8090`, Swagger UI on `8091`.

## Order lifecycle

1. A customer posts an order with an `Idempotency-Key`.
2. `order-service` fetches current prices from `catalog-service`, snapshots them, and stores the order as `PLACED` together with an outbox event.
3. `payment-service` consumes `order.placed`, simulates a payment, and publishes `payment.succeeded` or `payment.failed`.
4. `order-service` consumes the payment result and publishes `order.paid` or `order.payment-failed`.
5. `kitchen-service` consumes `order.paid`, creates a ticket, and emits `kitchen.ticket-accepted`, `kitchen.preparation-started`, and `kitchen.order-ready` over time.
6. `order-service` applies each kitchen event and publishes `order.status-changed`.
7. `notification-service` consumes `order.status-changed` and stores a simulated notification.
8. Customers can cancel before preparation. Staff complete an order once it is `READY`.

Statuses: `PLACED -> PAID -> CONFIRMED -> PREPARING -> READY -> COMPLETED`, with `CANCELLED` and `PAYMENT_FAILED` as terminal outcomes.

## Prerequisites

- Java 25 (`JAVA_HOME` must point to a JDK 25)
- Podman and `podman compose` (the repository is developed with Podman 5.x and `podman-compose` 1.6)
- Python 3 for the smoke-test script
- Network access for the Maven wrapper and container images

No local Maven installation is required; use `./mvnw`.

## Quick start with Podman

```bash
cp .env.example .env
podman compose up -d --build
```

Wait until all service health checks are green:

```bash
podman compose ps
```

Then open:

- Swagger UI: <http://localhost:8091>
- Kafka UI: <http://localhost:8090>
- Service health: <http://localhost:8081/actuator/health> through <http://localhost:8085/actuator/health>

Run the end-to-end smoke test:

```bash
./scripts/e2e-smoke.sh
```

Stop the stack:

```bash
podman compose down
```

Use `podman compose down -v` to also delete the PostgreSQL and Kafka volumes.

## Running services locally

Start only the infrastructure, then build once and run services from Maven:

```bash
podman compose up -d postgres kafka kafka-init kafka-ui swagger-ui
./mvnw -DskipTests install
./mvnw -pl services/catalog-service spring-boot:run
./mvnw -pl services/order-service spring-boot:run
./mvnw -pl services/kitchen-service spring-boot:run
./mvnw -pl services/payment-service spring-boot:run
./mvnw -pl services/notification-service spring-boot:run
```

The default `application.yml` files use `localhost:5432` and `localhost:9092`. Each service has its own database: `catalog_db`, `order_db`, `kitchen_db`, `payment_db`, `notification_db`.

## API examples

Create an order:

```bash
curl -s -X POST http://localhost:8082/api/v1/orders \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: demo-order-001' \
  -d '{
    "customerId": "customer-1",
    "items": [
      {"pizzaId": "11111111-1111-1111-1111-111111111111", "size": "LARGE", "quantity": 2}
    ],
    "deliveryAddress": {"street": "Main Street 1", "city": "Pizza Town", "postalCode": "12345"}
  }'
```

Replaying the same key and body returns the original order with HTTP 200. Reusing the key with a different body returns HTTP 409.

Read order state and history:

```bash
curl -s http://localhost:8082/api/v1/orders/{orderId}
curl -s http://localhost:8082/api/v1/orders/{orderId}/history
```

Cancel or complete an order:

```bash
curl -s -X POST http://localhost:8082/api/v1/orders/{orderId}/cancel \
  -H 'Content-Type: application/json' \
  -d '{"reason": "Changed my mind"}'

curl -s -X POST http://localhost:8082/api/v1/admin/orders/{orderId}/complete
```

Read the other services:

```bash
curl -s http://localhost:8081/api/v1/pizzas
curl -s http://localhost:8083/api/v1/kitchen/tickets
curl -s http://localhost:8084/api/v1/payments/orders/{orderId}
curl -s "http://localhost:8085/api/v1/notifications?customerId=customer-1"
```

Create or update menu items through the admin endpoints:

```bash
curl -s -X POST http://localhost:8081/api/v1/admin/pizzas \
  -H 'Content-Type: application/json' \
  -d '{"name":"Funghi","description":"Mushrooms","available":true,"prices":{"SMALL":8.00,"MEDIUM":10.00,"LARGE":12.00}}'
```

## Configuration

The most useful environment variables are listed in `.env.example`:

| Variable | Default | Purpose |
|---|---|---|
| `POSTGRES_USER` / `POSTGRES_PASSWORD` | `pizzashop` | Dev database credentials |
| `CATALOG_SERVICE_PORT` ... `NOTIFICATION_SERVICE_PORT` | `8081` ... `8085` | Host ports |
| `KAFKA_UI_PORT` / `SWAGGER_UI_PORT` | `8090` / `8091` | Tooling ports |
| `PAYMENT_MODE` | `SUCCESS` | `SUCCESS`, `FAILURE` or `RANDOM` |
| `PAYMENT_FAILURE_RATE` | `0.0` | Failure rate for `RANDOM` mode |
| `PAYMENT_DELAY_MS` | `1500` | Simulated payment delay |
| `KITCHEN_ACCEPT_DELAY_MS` | `1000` | Delay before preparation starts |
| `KITCHEN_PREPARATION_DELAY_MS` | `4000` | Simulated preparation duration |

All service settings can also be overridden with standard Spring Boot environment variables, for example `SPRING_DATASOURCE_URL` or `APP_PAYMENT_MODE`.

## Reliability patterns

- **Transactional outbox:** domain changes and integration events are committed in the same database transaction. A scheduled publisher sends due rows with `FOR UPDATE SKIP LOCKED`, retries with backoff, and records failures.
- **Inbox / idempotent consumers:** each consumer records processed event IDs in the same transaction as its side effects. Duplicate Kafka deliveries are ignored.
- **Idempotent order creation:** the `Idempotency-Key` plus a SHA-256 request hash makes order creation safe to retry.
- **Dead-letter topics:** malformed or non-retryable events go to `<topic>.DLT`; transient errors are retried.
- **Ordering:** events are keyed by `orderId`, and all order commands are published on `order.events.v1` so payment and kitchen see them in order.
- **Restart-safe kitchen timers:** ticket deadlines are stored in PostgreSQL and processed by a scheduled poller, not in-memory timers.

The system is at-least-once end to end; idempotent consumers make side effects effectively once.

## Testing

Fast unit tests:

```bash
./mvnw test
```

Integration tests with Testcontainers (PostgreSQL + Kafka):

```bash
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk
export DOCKER_HOST=unix:///run/user/$(id -u)/podman/podman.sock
export TESTCONTAINERS_RYUK_DISABLED=true
export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/run/user/$(id -u)/podman/podman.sock
./mvnw verify -Pintegration
```

Start the Podman service socket first if it is not running:

```bash
podman system service --time=0 "unix:///run/user/$(id -u)/podman/podman.sock" &
```

The end-to-end smoke test expects a running stack:

```bash
./scripts/e2e-smoke.sh
```

## Project structure

```
.
├── openapi/                 # Contract-first OpenAPI specifications
├── shared/
│   ├── contracts/           # Event envelope, payload records, JSON Schema
│   ├── messaging/           # Outbox, inbox, Kafka error handling, auto-config
│   ├── web-support/         # ProblemDetail advice, correlation IDs
│   └── test-support/        # Shared Testcontainers configuration
├── services/
│   ├── catalog-service/
│   ├── order-service/
│   ├── kitchen-service/
│   ├── payment-service/
│   └── notification-service/
├── infra/                   # PostgreSQL init and Kafka topic scripts
├── scripts/e2e-smoke.sh
├── docs/                    # Architecture, event catalog and ADRs
├── compose.yaml
└── Containerfile            # Shared multi-stage service image
```

Each service uses the same hexagonal layout:

```
domain/model, domain/service
application/port/in, application/port/out, application/service
adapter/in/rest, adapter/in/kafka
adapter/out/persistence, adapter/out/messaging, adapter/out/client
config
```

## Troubleshooting

- **Short-name image resolution under Podman:** all images are prefixed with `docker.io/`. Keep that prefix if you add images.
- **Testcontainers cannot find Docker:** set `DOCKER_HOST` to the rootless Podman socket and start `podman system service` as shown above.
- **Ryuk fails on rootless Podman:** set `TESTCONTAINERS_RYUK_DISABLED=true` for local test runs.
- **SELinux bind mounts:** compose mounts use the `:Z` suffix.
- **Port already in use:** override the ports in `.env`.
- **Do not rebuild a running jar:** replacing a Spring Boot jar while a JVM is running can cause `NoClassDefFoundError` when classes are loaded lazily. Stop the process before packaging.
- **Flyway and the shared messaging tables:** the repeatable migration `R__messaging_tables.sql` is packaged in `shared-messaging` and discovered from each service's `classpath:db/migration`.

## Known limitations

- Admin endpoints are unsecured in v1. Authentication and authorization are a follow-up.
- Payment and kitchen processing are simulations; no external provider is integrated.
- Delivery is not implemented; staff complete a `READY` order through the admin endpoint.
- Local development uses one PostgreSQL container with separate databases. Production should use separate instances and credentials.
- There is no API gateway; each service exposes its own port.

## Further documentation

- [Architecture](docs/architecture.md)
- [Event catalog](docs/events.md)
- [Architecture decision records](docs/adr/)
