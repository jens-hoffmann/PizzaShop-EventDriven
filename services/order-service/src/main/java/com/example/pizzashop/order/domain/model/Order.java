package com.example.pizzashop.order.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class Order {

    private final UUID id;
    private final String customerId;
    private OrderStatus status;
    private final List<OrderItem> items;
    private final BigDecimal total;
    private final String currency;
    private final DeliveryAddress deliveryAddress;
    private final Instant createdAt;
    private Instant updatedAt;
    private long version;

    private Order(
            UUID id,
            String customerId,
            OrderStatus status,
            List<OrderItem> items,
            BigDecimal total,
            String currency,
            DeliveryAddress deliveryAddress,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.items = List.copyOf(items);
        this.total = total;
        this.currency = currency;
        this.deliveryAddress = deliveryAddress;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public static Order place(
            UUID id,
            String customerId,
            List<OrderItem> items,
            DeliveryAddress deliveryAddress,
            Instant now
    ) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId is required");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("An order must contain at least one item");
        }
        BigDecimal total = items.stream()
                .map(OrderItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        return new Order(
                id,
                customerId,
                OrderStatus.PLACED,
                items,
                total,
                "EUR",
                deliveryAddress,
                now,
                now,
                0
        );
    }

    public static Order reconstitute(
            UUID id,
            String customerId,
            OrderStatus status,
            List<OrderItem> items,
            BigDecimal total,
            String currency,
            DeliveryAddress deliveryAddress,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        return new Order(
                id,
                customerId,
                status,
                items,
                total,
                currency,
                deliveryAddress,
                createdAt,
                updatedAt,
                version
        );
    }

    public void markPaid(Instant now) {
        transitionTo(OrderStatus.PAID, now);
    }

    public void markPaymentFailed(Instant now) {
        transitionTo(OrderStatus.PAYMENT_FAILED, now);
    }

    public void confirm(Instant now) {
        transitionTo(OrderStatus.CONFIRMED, now);
    }

    public void startPreparation(Instant now) {
        transitionTo(OrderStatus.PREPARING, now);
    }

    public void markReady(Instant now) {
        transitionTo(OrderStatus.READY, now);
    }

    public void cancel(Instant now) {
        transitionTo(OrderStatus.CANCELLED, now);
    }

    public void complete(Instant now) {
        transitionTo(OrderStatus.COMPLETED, now);
    }

    public boolean canBeCancelled() {
        return status == OrderStatus.PLACED
                || status == OrderStatus.PAID
                || status == OrderStatus.CONFIRMED;
    }

    public boolean wasPaid() {
        return status == OrderStatus.PAID
                || status == OrderStatus.CONFIRMED
                || status == OrderStatus.PREPARING
                || status == OrderStatus.READY;
    }

    private void transitionTo(OrderStatus target, Instant now) {
        if (!isAllowed(status, target)) {
            throw new InvalidOrderTransitionException(status, target);
        }
        this.status = target;
        this.updatedAt = now;
    }

    private static boolean isAllowed(OrderStatus from, OrderStatus to) {
        return switch (from) {
            case PLACED -> to == OrderStatus.PAID
                    || to == OrderStatus.PAYMENT_FAILED
                    || to == OrderStatus.CANCELLED;
            case PAID -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED -> to == OrderStatus.PREPARING || to == OrderStatus.CANCELLED;
            case PREPARING -> to == OrderStatus.READY;
            case READY -> to == OrderStatus.COMPLETED;
            default -> false;
        };
    }

    public UUID id() {
        return id;
    }

    public String customerId() {
        return customerId;
    }

    public OrderStatus status() {
        return status;
    }

    public List<OrderItem> items() {
        return items;
    }

    public BigDecimal total() {
        return total;
    }

    public String currency() {
        return currency;
    }

    public DeliveryAddress deliveryAddress() {
        return deliveryAddress;
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
}
