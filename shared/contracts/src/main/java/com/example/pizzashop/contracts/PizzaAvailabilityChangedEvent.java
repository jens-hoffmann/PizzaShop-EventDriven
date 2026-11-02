package com.example.pizzashop.contracts;

import java.time.Instant;
import java.util.UUID;

public record PizzaAvailabilityChangedEvent(
        UUID pizzaId,
        boolean available,
        Instant updatedAt
) {
}
