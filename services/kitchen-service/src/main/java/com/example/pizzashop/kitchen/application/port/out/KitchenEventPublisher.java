package com.example.pizzashop.kitchen.application.port.out;

import com.example.pizzashop.kitchen.domain.model.KitchenTicket;

public interface KitchenEventPublisher {

    void ticketAccepted(KitchenTicket ticket);

    void preparationStarted(KitchenTicket ticket);

    void orderReady(KitchenTicket ticket);

    void ticketCancelled(KitchenTicket ticket, String reason);
}
