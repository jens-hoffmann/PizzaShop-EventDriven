package com.example.pizzashop.notification.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Notification(
        UUID id,
        UUID orderId,
        String customerId,
        String status,
        String message,
        Instant createdAt
) {
}
