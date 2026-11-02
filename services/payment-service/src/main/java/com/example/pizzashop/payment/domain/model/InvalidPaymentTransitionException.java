package com.example.pizzashop.payment.domain.model;

public class InvalidPaymentTransitionException extends RuntimeException {

    public InvalidPaymentTransitionException(PaymentStatus status, String action) {
        super("Cannot " + action + " payment in status " + status);
    }
}
