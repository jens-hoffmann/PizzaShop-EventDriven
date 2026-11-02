package com.example.pizzashop.contracts;

import java.time.Instant;
import java.util.UUID;

public record OrderCompletedEvent(
        UUID orderId,
        Instant completedAt
) {
}
