package com.example.pizzashop.contracts;

public final class Topics {

    public static final String CATALOG_EVENTS = "catalog.events.v1";
    public static final String ORDER_EVENTS = "order.events.v1";
    public static final String PAYMENT_EVENTS = "payment.events.v1";
    public static final String KITCHEN_EVENTS = "kitchen.events.v1";

    private Topics() {
    }

    public static String deadLetter(String topic) {
        return topic + ".DLT";
    }

    public static String topicFor(String eventType) {
        if (eventType.startsWith("catalog.")) {
            return CATALOG_EVENTS;
        }
        if (eventType.startsWith("order.")) {
            return ORDER_EVENTS;
        }
        if (eventType.startsWith("payment.")) {
            return PAYMENT_EVENTS;
        }
        if (eventType.startsWith("kitchen.")) {
            return KITCHEN_EVENTS;
        }
        throw new IllegalArgumentException("Unknown event type: " + eventType);
    }
}
