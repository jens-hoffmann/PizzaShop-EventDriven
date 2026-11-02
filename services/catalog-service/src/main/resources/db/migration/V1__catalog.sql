CREATE TABLE pizza (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    available BOOLEAN NOT NULL,
    small_price NUMERIC(10, 2) NOT NULL,
    medium_price NUMERIC(10, 2) NOT NULL,
    large_price NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_pizza_name UNIQUE (name)
);

CREATE INDEX idx_pizza_available ON pizza (available);
