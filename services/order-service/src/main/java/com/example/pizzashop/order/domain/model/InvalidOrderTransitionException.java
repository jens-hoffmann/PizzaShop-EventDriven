package com.example.pizzashop.order.domain.model;

public class InvalidOrderTransitionException extends RuntimeException {

    public InvalidOrderTransitionException(OrderStatus from, OrderStatus to) {
        super("Cannot transition order from " + from + " to " + to);
    }
}
