package com.example.pizzashop.contracts;

import java.time.Instant;
import java.util.UUID;

public record OrderPaidEvent(
        UUID orderId,
        UUID paymentId,
        Instant paidAt
) {
}
