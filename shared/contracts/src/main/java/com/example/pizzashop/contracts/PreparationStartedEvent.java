package com.example.pizzashop.contracts;

import java.time.Instant;
import java.util.UUID;

public record PreparationStartedEvent(
        UUID ticketId,
        UUID orderId,
        Instant startedAt
) {
}
