package com.example.pizzashop.contracts;

import java.time.Instant;
import java.util.UUID;

public record OrderCancelledEvent(
        UUID orderId,
        String reason,
        Instant cancelledAt,
        boolean wasPaid
) {
}
