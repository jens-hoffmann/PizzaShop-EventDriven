package com.example.pizzashop.catalog.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class Pizza {

    private final UUID id;
    private String name;
    private String description;
    private boolean available;
    private Map<PizzaSize, BigDecimal> prices;
    private final Instant createdAt;
    private Instant updatedAt;
    private long version;

    private Pizza(
            UUID id,
            String name,
            String description,
            boolean available,
            Map<PizzaSize, BigDecimal> prices,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
        this.prices = copyPrices(prices);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public static Pizza create(
            UUID id,
            String name,
            String description,
            boolean available,
            Map<PizzaSize, BigDecimal> prices,
            Instant now
    ) {
        validateName(name);
        validatePrices(prices);
        return new Pizza(id, name, description, available, prices, now, now, 0);
    }

    public static Pizza reconstitute(
            UUID id,
            String name,
            String description,
            boolean available,
            Map<PizzaSize, BigDecimal> prices,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        return new Pizza(id, name, description, available, prices, createdAt, updatedAt, version);
    }

    public void update(
            String name,
            String description,
            boolean available,
            Map<PizzaSize, BigDecimal> prices,
            Instant now
    ) {
        validateName(name);
        validatePrices(prices);
        this.name = name;
        this.description = description;
        this.available = available;
        this.prices = copyPrices(prices);
        this.updatedAt = now;
    }

    public void changeAvailability(boolean available, Instant now) {
        this.available = available;
        this.updatedAt = now;
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public boolean available() {
        return available;
    }

    public Map<PizzaSize, BigDecimal> prices() {
        return Collections.unmodifiableMap(prices);
    }

    public BigDecimal priceFor(PizzaSize size) {
        return prices.get(size);
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public long version() {
        return version;
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Pizza name must not be blank");
        }
        if (name.length() > 120) {
            throw new IllegalArgumentException("Pizza name must not exceed 120 characters");
        }
    }

    private static void validatePrices(Map<PizzaSize, BigDecimal> prices) {
        if (prices == null || prices.size() != PizzaSize.values().length) {
            throw new IllegalArgumentException("Prices must be provided for SMALL, MEDIUM and LARGE");
        }
        for (PizzaSize size : PizzaSize.values()) {
            BigDecimal price = prices.get(size);
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Price for " + size + " must be positive");
            }
        }
    }

    private static Map<PizzaSize, BigDecimal> copyPrices(Map<PizzaSize, BigDecimal> prices) {
        EnumMap<PizzaSize, BigDecimal> copy = new EnumMap<>(PizzaSize.class);
        copy.putAll(Objects.requireNonNull(prices, "prices"));
        return copy;
    }
}
