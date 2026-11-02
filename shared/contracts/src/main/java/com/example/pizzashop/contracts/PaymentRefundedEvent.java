package com.example.pizzashop.contracts;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentRefundedEvent(
        UUID paymentId,
        UUID orderId,
        BigDecimal amount,
        Instant refundedAt,
        String reason
) {
}
