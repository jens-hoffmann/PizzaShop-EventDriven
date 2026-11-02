package com.example.pizzashop.contracts;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderPlacedEvent(
        UUID orderId,
        String customerId,
        List<OrderLineSnapshot> items,
        BigDecimal total,
        String currency,
        DeliveryAddressSnapshot deliveryAddress,
        Instant placedAt
) {
}
