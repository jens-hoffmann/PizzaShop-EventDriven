package com.example.pizzashop.contracts;

import java.time.Instant;
import java.util.UUID;

public record KitchenTicketCancelledEvent(
        UUID ticketId,
        UUID orderId,
        String reason,
        Instant cancelledAt
) {
}
