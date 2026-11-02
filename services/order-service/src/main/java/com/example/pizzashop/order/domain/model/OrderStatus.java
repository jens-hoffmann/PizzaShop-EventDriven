package com.example.pizzashop.order.domain.model;

public enum OrderStatus {
    PLACED,
    PAID,
    CONFIRMED,
    PREPARING,
    READY,
    COMPLETED,
    CANCELLED,
    PAYMENT_FAILED;

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED || this == PAYMENT_FAILED;
    }
}
