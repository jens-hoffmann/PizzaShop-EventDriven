CREATE TABLE IF NOT EXISTS outbox_message (
    id UUID PRIMARY KEY,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_version INTEGER NOT NULL,
    payload TEXT NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ,
    attempts INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ,
    last_error TEXT
);

CREATE INDEX IF NOT EXISTS idx_outbox_message_due
    ON outbox_message (created_at)
    WHERE published_at IS NULL;

CREATE TABLE IF NOT EXISTS processed_event (
    event_id UUID NOT NULL,
    consumer VARCHAR(100) NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (event_id, consumer)
);
