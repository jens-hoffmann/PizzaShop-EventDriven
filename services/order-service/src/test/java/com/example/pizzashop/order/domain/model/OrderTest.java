package com.example.pizzashop.order.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final Instant NOW = Instant.parse("2026-11-01T12:00:00Z");

    @Test
    void placesAnOrderAndComputesTheTotal() {
        Order order = Order.place(
                UUID.randomUUID(),
                "customer-1",
                List.of(
                        OrderItem.create(UUID.randomUUID(), "Margherita", "LARGE", new BigDecimal("11.50"), 2),
                        OrderItem.create(UUID.randomUUID(), "Diavola", "SMALL", new BigDecimal("8.50"), 1)
                ),
                new DeliveryAddress("Main Street 1", "Pizza Town", "12345"),
                NOW
        );

        assertThat(order.status()).isEqualTo(OrderStatus.PLACED);
        assertThat(order.total()).isEqualByComparingTo("31.50");
        assertThat(order.currency()).isEqualTo("EUR");
    }

    @Test
    void followsTheHappyPathStateMachine() {
        Order order = placedOrder();

        order.markPaid(NOW.plusSeconds(1));
        order.confirm(NOW.plusSeconds(2));
        order.startPreparation(NOW.plusSeconds(3));
        order.markReady(NOW.plusSeconds(4));
        order.complete(NOW.plusSeconds(5));

        assertThat(order.status()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void allowsCancellationBeforePreparation() {
        Order order = placedOrder();
        order.markPaid(NOW.plusSeconds(1));
        order.confirm(NOW.plusSeconds(2));

        order.cancel(NOW.plusSeconds(3));

        assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void rejectsCancellationDuringPreparation() {
        Order order = placedOrder();
        order.markPaid(NOW.plusSeconds(1));
        order.confirm(NOW.plusSeconds(2));
        order.startPreparation(NOW.plusSeconds(3));

        assertThatThrownBy(() -> order.cancel(NOW.plusSeconds(4)))
                .isInstanceOf(InvalidOrderTransitionException.class);
    }

    @Test
    void rejectsAnInvalidTransition() {
        Order order = placedOrder();

        assertThatThrownBy(() -> order.markReady(NOW.plusSeconds(1)))
                .isInstanceOf(InvalidOrderTransitionException.class);
    }

    private Order placedOrder() {
        return Order.place(
                UUID.randomUUID(),
                "customer-1",
                List.of(OrderItem.create(
                        UUID.randomUUID(), "Margherita", "LARGE", new BigDecimal("11.50"), 1)),
                new DeliveryAddress("Main Street 1", "Pizza Town", "12345"),
                NOW
        );
    }
}
