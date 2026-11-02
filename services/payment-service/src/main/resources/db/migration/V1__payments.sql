CREATE TABLE payment (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE,
    amount NUMERIC(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(32) NOT NULL,
    failure_reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_payment_order ON payment (order_id);
CREATE INDEX idx_payment_status ON payment (status);
