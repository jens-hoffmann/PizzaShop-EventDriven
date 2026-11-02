package com.example.pizzashop.contracts;

public final class EventTypes {

    public static final String PIZZA_CREATED = "catalog.pizza-created";
    public static final String PIZZA_UPDATED = "catalog.pizza-updated";
    public static final String PIZZA_AVAILABILITY_CHANGED = "catalog.pizza-availability-changed";

    public static final String ORDER_PLACED = "order.placed";
    public static final String ORDER_PAID = "order.paid";
    public static final String ORDER_PAYMENT_FAILED = "order.payment-failed";
    public static final String ORDER_CANCELLED = "order.cancelled";
    public static final String ORDER_COMPLETED = "order.completed";
    public static final String ORDER_STATUS_CHANGED = "order.status-changed";

    public static final String PAYMENT_SUCCEEDED = "payment.succeeded";
    public static final String PAYMENT_FAILED = "payment.failed";
    public static final String PAYMENT_REFUNDED = "payment.refunded";

    public static final String KITCHEN_TICKET_ACCEPTED = "kitchen.ticket-accepted";
    public static final String KITCHEN_PREPARATION_STARTED = "kitchen.preparation-started";
    public static final String KITCHEN_ORDER_READY = "kitchen.order-ready";
    public static final String KITCHEN_TICKET_CANCELLED = "kitchen.ticket-cancelled";

    private EventTypes() {
    }
}
