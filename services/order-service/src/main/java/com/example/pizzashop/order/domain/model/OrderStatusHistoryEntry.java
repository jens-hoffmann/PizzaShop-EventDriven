package com.example.pizzashop.order.domain.model;

import java.time.Instant;
import java.util.UUID;

public record OrderStatusHistoryEntry(
        UUID orderId,
        OrderStatus status,
        String reason,
        Instant occurredAt,
        UUID sourceEventId
) {
}
