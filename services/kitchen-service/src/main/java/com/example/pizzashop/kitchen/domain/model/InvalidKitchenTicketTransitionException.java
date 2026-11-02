package com.example.pizzashop.kitchen.domain.model;

public class InvalidKitchenTicketTransitionException extends RuntimeException {

    public InvalidKitchenTicketTransitionException(KitchenTicketStatus status, String action) {
        super("Cannot " + action + " kitchen ticket in status " + status);
    }
}
