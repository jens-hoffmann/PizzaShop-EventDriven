package com.example.pizzashop.contracts;

import java.time.Instant;
import java.util.UUID;

public record OrderStatusChangedEvent(
        UUID orderId,
        String customerId,
        String previousStatus,
        String newStatus,
        String reason,
        Instant occurredAt
) {
}
