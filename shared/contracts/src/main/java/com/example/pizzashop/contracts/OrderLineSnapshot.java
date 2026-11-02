package com.example.pizzashop.contracts;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderLineSnapshot(
        UUID pizzaId,
        String name,
        String size,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
}
