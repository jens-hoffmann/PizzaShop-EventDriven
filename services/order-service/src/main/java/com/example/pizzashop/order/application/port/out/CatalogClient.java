package com.example.pizzashop.order.application.port.out;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface CatalogClient {

    Map<UUID, CatalogPizza> findPizzas(Set<UUID> pizzaIds);

    record CatalogPizza(
            UUID id,
            String name,
            boolean available,
            Map<String, BigDecimal> prices
    ) {
    }
}
