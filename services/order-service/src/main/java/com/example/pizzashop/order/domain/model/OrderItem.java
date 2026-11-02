package com.example.pizzashop.order.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import java.util.UUID;

public record OrderItem(
        UUID pizzaId,
        String name,
        String size,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {

    private static final Set<String> SIZES = Set.of("SMALL", "MEDIUM", "LARGE");

    public static OrderItem create(UUID pizzaId, String name, String size, BigDecimal unitPrice, int quantity) {
        if (pizzaId == null) {
            throw new IllegalArgumentException("pizzaId is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Pizza name is required");
        }
        if (size == null || !SIZES.contains(size)) {
            throw new IllegalArgumentException("Unsupported pizza size: " + size);
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
        if (quantity < 1 || quantity > 20) {
            throw new IllegalArgumentException("Quantity must be between 1 and 20");
        }
        BigDecimal normalizedUnitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
        BigDecimal lineTotal = normalizedUnitPrice
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
        return new OrderItem(pizzaId, name, size, normalizedUnitPrice, quantity, lineTotal);
    }
}
