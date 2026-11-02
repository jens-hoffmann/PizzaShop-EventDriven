package com.example.pizzashop.contracts;

import java.time.Instant;
import java.util.UUID;

public record PaymentFailedEvent(
        UUID paymentId,
        UUID orderId,
        String reason,
        Instant failedAt
) {
}
