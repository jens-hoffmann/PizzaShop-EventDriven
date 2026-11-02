package com.example.pizzashop.contracts;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentSucceededEvent(
        UUID paymentId,
        UUID orderId,
        BigDecimal amount,
        String currency,
        Instant paidAt
) {
}
