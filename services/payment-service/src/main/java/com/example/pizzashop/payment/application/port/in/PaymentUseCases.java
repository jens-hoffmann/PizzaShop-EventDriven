package com.example.pizzashop.payment.application.port.in;

import com.example.pizzashop.contracts.EventEnvelope;
import com.example.pizzashop.contracts.OrderCancelledEvent;
import com.example.pizzashop.contracts.OrderPlacedEvent;
import com.example.pizzashop.payment.domain.model.Payment;

import java.util.UUID;

public interface PaymentUseCases {

    Payment getByOrderId(UUID orderId);

    void onOrderPlaced(EventEnvelope<OrderPlacedEvent> event);

    void onOrderCancelled(EventEnvelope<OrderCancelledEvent> event);
}
