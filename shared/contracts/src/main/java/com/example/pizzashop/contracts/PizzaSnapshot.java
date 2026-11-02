package com.example.pizzashop.contracts;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record PizzaSnapshot(
        UUID pizzaId,
        String name,
        String description,
        boolean available,
        Map<String, BigDecimal> prices,
        Instant updatedAt
) {
}
