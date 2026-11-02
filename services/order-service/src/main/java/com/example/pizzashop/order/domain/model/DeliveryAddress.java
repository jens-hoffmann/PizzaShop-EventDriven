package com.example.pizzashop.order.domain.model;

public record DeliveryAddress(String street, String city, String postalCode) {

    public DeliveryAddress {
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("Street is required");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City is required");
        }
        if (postalCode == null || postalCode.isBlank()) {
            throw new IllegalArgumentException("Postal code is required");
        }
    }
}
