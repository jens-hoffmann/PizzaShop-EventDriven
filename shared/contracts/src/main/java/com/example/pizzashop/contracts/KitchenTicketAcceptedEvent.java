package com.example.pizzashop.contracts;

import java.time.Instant;
import java.util.UUID;

public record KitchenTicketAcceptedEvent(
        UUID ticketId,
        UUID orderId,
        Instant acceptedAt
) {
}
