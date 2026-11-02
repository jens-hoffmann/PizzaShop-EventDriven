package com.example.pizzashop.kitchen.application.port.out;

import com.example.pizzashop.kitchen.domain.model.KitchenTicket;
import com.example.pizzashop.kitchen.domain.model.KitchenTicketStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KitchenTicketRepository {

    KitchenTicket save(KitchenTicket ticket);

    Optional<KitchenTicket> findById(UUID id);

    Optional<KitchenTicket> findByOrderId(UUID orderId);

    List<KitchenTicket> findAll(Optional<KitchenTicketStatus> status);

    List<KitchenTicket> findDue(Instant now, int limit);
}
