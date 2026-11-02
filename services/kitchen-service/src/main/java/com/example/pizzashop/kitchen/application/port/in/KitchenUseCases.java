package com.example.pizzashop.kitchen.application.port.in;

import com.example.pizzashop.contracts.EventEnvelope;
import com.example.pizzashop.contracts.OrderCancelledEvent;
import com.example.pizzashop.contracts.OrderPaidEvent;
import com.example.pizzashop.kitchen.domain.model.KitchenTicket;
import com.example.pizzashop.kitchen.domain.model.KitchenTicketStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KitchenUseCases {

    List<KitchenTicket> list(Optional<KitchenTicketStatus> status);

    KitchenTicket get(UUID id);

    void onOrderPaid(EventEnvelope<OrderPaidEvent> event);

    void onOrderCancelled(EventEnvelope<OrderCancelledEvent> event);

    void processDueTickets();
}
