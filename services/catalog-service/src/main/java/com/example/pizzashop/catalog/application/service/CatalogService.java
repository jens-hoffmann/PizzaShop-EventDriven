package com.example.pizzashop.catalog.application.service;

import com.example.pizzashop.catalog.application.port.in.CatalogUseCases;
import com.example.pizzashop.catalog.application.port.out.CatalogEventPublisher;
import com.example.pizzashop.catalog.application.port.out.PizzaRepository;
import com.example.pizzashop.catalog.domain.model.Pizza;
import com.example.pizzashop.catalog.domain.model.PizzaSize;
import com.example.pizzashop.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class CatalogService implements CatalogUseCases {

    private final PizzaRepository repository;
    private final CatalogEventPublisher eventPublisher;
    private final Clock clock;

    public CatalogService(
            PizzaRepository repository,
            CatalogEventPublisher eventPublisher,
            Clock clock
    ) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pizza> list(Optional<List<UUID>> ids, Optional<Boolean> available) {
        return repository.findAll(ids, available);
    }

    @Override
    @Transactional(readOnly = true)
    public Pizza get(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> notFound(id));
    }

    @Override
    @Transactional
    public Pizza create(CreatePizzaCommand command) {
        String name = normalizeName(command.name());
        if (repository.findByNameIgnoreCase(name).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "PIZZA_NAME_EXISTS",
                    "A pizza with name '" + name + "' already exists");
        }
        Pizza pizza = Pizza.create(
                UUID.randomUUID(),
                name,
                command.description(),
                command.available(),
                toPrices(command.prices()),
                Instant.now(clock)
        );
        Pizza saved = repository.save(pizza);
        eventPublisher.pizzaCreated(saved);
        return saved;
    }

    @Override
    @Transactional
    public Pizza update(UUID id, UpdatePizzaCommand command) {
        Pizza pizza = get(id);
        String name = normalizeName(command.name());
        repository.findByNameIgnoreCase(name)
                .filter(existing -> !existing.id().equals(id))
                .ifPresent(existing -> {
                    throw new ApiException(HttpStatus.CONFLICT, "PIZZA_NAME_EXISTS",
                            "A pizza with name '" + name + "' already exists");
                });
        pizza.update(name, command.description(), command.available(), toPrices(command.prices()), Instant.now(clock));
        Pizza saved = repository.save(pizza);
        eventPublisher.pizzaUpdated(saved);
        return saved;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Pizza pizza = get(id);
        repository.delete(pizza);
    }

    @Override
    @Transactional
    public Pizza changeAvailability(UUID id, boolean available) {
        Pizza pizza = get(id);
        if (pizza.available() == available) {
            return pizza;
        }
        pizza.changeAvailability(available, Instant.now(clock));
        Pizza saved = repository.save(pizza);
        eventPublisher.pizzaAvailabilityChanged(saved);
        return saved;
    }

    private Map<PizzaSize, BigDecimal> toPrices(Map<String, BigDecimal> rawPrices) {
        if (rawPrices == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PRICES", "Prices are required");
        }
        EnumMap<PizzaSize, BigDecimal> prices = new EnumMap<>(PizzaSize.class);
        for (PizzaSize size : PizzaSize.values()) {
            BigDecimal price = rawPrices.get(size.name());
            if (price == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PRICES",
                        "Missing price for size " + size);
            }
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PRICES",
                        "Price for size " + size + " must be positive");
            }
            prices.put(size, price.setScale(2, RoundingMode.HALF_UP));
        }
        if (rawPrices.size() != PizzaSize.values().length) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PRICES",
                    "Only SMALL, MEDIUM and LARGE prices are supported");
        }
        return prices;
    }

    private static String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_NAME", "Pizza name must not be blank");
        }
        return name.trim();
    }

    private static ApiException notFound(UUID id) {
        return new ApiException(HttpStatus.NOT_FOUND, "PIZZA_NOT_FOUND", "Pizza " + id + " not found");
    }
}
