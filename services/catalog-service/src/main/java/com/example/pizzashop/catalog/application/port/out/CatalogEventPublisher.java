package com.example.pizzashop.catalog.application.port.out;

import com.example.pizzashop.catalog.domain.model.Pizza;

public interface CatalogEventPublisher {

    void pizzaCreated(Pizza pizza);

    void pizzaUpdated(Pizza pizza);

    void pizzaAvailabilityChanged(Pizza pizza);
}
