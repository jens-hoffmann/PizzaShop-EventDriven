package com.example.pizzashop.catalog.application.port.in;

import com.example.pizzashop.catalog.domain.model.Pizza;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface CatalogUseCases {

    List<Pizza> list(Optional<List<UUID>> ids, Optional<Boolean> available);

    Pizza get(UUID id);

    Pizza create(CreatePizzaCommand command);

    Pizza update(UUID id, UpdatePizzaCommand command);

    void delete(UUID id);

    Pizza changeAvailability(UUID id, boolean available);

    record CreatePizzaCommand(
            String name,
            String description,
            boolean available,
            Map<String, BigDecimal> prices
    ) {
    }

    record UpdatePizzaCommand(
            String name,
            String description,
            boolean available,
            Map<String, BigDecimal> prices
    ) {
    }
}
