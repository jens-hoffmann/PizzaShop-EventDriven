package com.example.pizzashop.catalog.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PizzaTest {

    private static final Instant NOW = Instant.parse("2026-11-01T12:00:00Z");

    @Test
    void createsAPizzaWithAllRequiredPrices() {
        Pizza pizza = Pizza.create(
                UUID.randomUUID(),
                "Margherita",
                "Tomato and mozzarella",
                true,
                prices("7.50", "9.50", "11.50"),
                NOW
        );

        assertThat(pizza.available()).isTrue();
        assertThat(pizza.priceFor(PizzaSize.LARGE)).isEqualByComparingTo("11.50");
        assertThat(pizza.createdAt()).isEqualTo(NOW);
    }

    @Test
    void rejectsMissingPrices() {
        Map<PizzaSize, BigDecimal> prices = new EnumMap<>(PizzaSize.class);
        prices.put(PizzaSize.SMALL, new BigDecimal("7.50"));

        assertThatThrownBy(() -> Pizza.create(UUID.randomUUID(), "Test", "", true, prices, NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SMALL, MEDIUM and LARGE");
    }

    @Test
    void rejectsNonPositivePrices() {
        assertThatThrownBy(() -> Pizza.create(
                UUID.randomUUID(),
                "Test",
                "",
                true,
                prices("0.00", "9.50", "11.50"),
                NOW
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updatesAvailabilityAndTimestamp() {
        Pizza pizza = Pizza.create(UUID.randomUUID(), "Test", "", true, prices("1.00", "2.00", "3.00"), NOW);
        Instant later = NOW.plusSeconds(60);

        pizza.changeAvailability(false, later);

        assertThat(pizza.available()).isFalse();
        assertThat(pizza.updatedAt()).isEqualTo(later);
    }

    private static Map<PizzaSize, BigDecimal> prices(String small, String medium, String large) {
        Map<PizzaSize, BigDecimal> prices = new EnumMap<>(PizzaSize.class);
        prices.put(PizzaSize.SMALL, new BigDecimal(small));
        prices.put(PizzaSize.MEDIUM, new BigDecimal(medium));
        prices.put(PizzaSize.LARGE, new BigDecimal(large));
        return prices;
    }
}
