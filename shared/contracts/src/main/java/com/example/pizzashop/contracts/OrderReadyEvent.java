package com.example.pizzashop.contracts;

import java.time.Instant;
import java.util.UUID;

public record OrderReadyEvent(
        UUID ticketId,
        UUID orderId,
        Instant readyAt
) {
}
