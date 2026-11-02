package com.example.pizzashop.catalog.application.port.out;

import com.example.pizzashop.catalog.domain.model.Pizza;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PizzaRepository {

    List<Pizza> findAll(Optional<List<UUID>> ids, Optional<Boolean> available);

    Optional<Pizza> findById(UUID id);

    Optional<Pizza> findByNameIgnoreCase(String name);

    Pizza save(Pizza pizza);

    void delete(Pizza pizza);

    boolean existsByNameIgnoreCase(String name);
}
