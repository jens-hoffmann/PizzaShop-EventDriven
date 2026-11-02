CREATE TABLE notification (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    customer_id VARCHAR(100) NOT NULL,
    status VARCHAR(32) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_notification_customer_created ON notification (customer_id, created_at DESC);
CREATE INDEX idx_notification_order ON notification (order_id);
