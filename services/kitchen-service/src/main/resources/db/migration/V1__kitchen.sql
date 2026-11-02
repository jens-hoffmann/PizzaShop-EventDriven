CREATE TABLE kitchen_ticket (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    accepted_at TIMESTAMPTZ NOT NULL,
    preparation_started_at TIMESTAMPTZ,
    ready_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    next_action_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_kitchen_ticket_due ON kitchen_ticket (next_action_at)
    WHERE status IN ('ACCEPTED', 'PREPARING');

CREATE INDEX idx_kitchen_ticket_status ON kitchen_ticket (status);
