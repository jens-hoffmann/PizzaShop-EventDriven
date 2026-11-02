CREATE TABLE customer_order (
    id UUID PRIMARY KEY,
    customer_id VARCHAR(100) NOT NULL,
    status VARCHAR(32) NOT NULL,
    total NUMERIC(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    street VARCHAR(200) NOT NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_customer_order_customer_created ON customer_order (customer_id, created_at DESC);
CREATE INDEX idx_customer_order_status ON customer_order (status);

CREATE TABLE order_item (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES customer_order (id) ON DELETE CASCADE,
    pizza_id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    size VARCHAR(16) NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    line_total NUMERIC(10, 2) NOT NULL
);

CREATE INDEX idx_order_item_order ON order_item (order_id);

CREATE TABLE order_status_history (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES customer_order (id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL,
    reason VARCHAR(500),
    occurred_at TIMESTAMPTZ NOT NULL,
    source_event_id UUID
);

CREATE INDEX idx_order_status_history_order ON order_status_history (order_id, occurred_at);

CREATE TABLE idempotency_key (
    idempotency_key VARCHAR(128) PRIMARY KEY,
    request_hash VARCHAR(64) NOT NULL,
    order_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_idempotency_key_expires ON idempotency_key (expires_at);
