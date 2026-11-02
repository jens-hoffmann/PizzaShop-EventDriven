package com.example.pizzashop.kitchen.application.service;

import com.example.pizzashop.contracts.EventEnvelope;
import com.example.pizzashop.contracts.OrderCancelledEvent;
import com.example.pizzashop.contracts.OrderPaidEvent;
import com.example.pizzashop.kitchen.application.port.in.KitchenUseCases;
import com.example.pizzashop.kitchen.application.port.out.KitchenEventPublisher;
import com.example.pizzashop.kitchen.application.port.out.KitchenTicketRepository;
import com.example.pizzashop.kitchen.config.KitchenProperties;
import com.example.pizzashop.kitchen.domain.model.KitchenTicket;
import com.example.pizzashop.kitchen.domain.model.KitchenTicketStatus;
import com.example.pizzashop.web.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class KitchenService implements KitchenUseCases {

    private static final Logger log = LoggerFactory.getLogger(KitchenService.class);

    private final KitchenTicketRepository repository;
    private final KitchenEventPublisher eventPublisher;
    private final KitchenProperties properties;
    private final Clock clock;

    public KitchenService(
            KitchenTicketRepository repository,
            KitchenEventPublisher eventPublisher,
            KitchenProperties properties,
            Clock clock
    ) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<KitchenTicket> list(Optional<KitchenTicketStatus> status) {
        return repository.findAll(status);
    }

    @Override
    @Transactional(readOnly = true)
    public KitchenTicket get(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "KITCHEN_TICKET_NOT_FOUND", "Kitchen ticket " + id + " not found"));
    }

    @Override
    @Transactional
    public void onOrderPaid(EventEnvelope<OrderPaidEvent> event) {
        UUID orderId = event.payload().orderId();
        if (repository.findByOrderId(orderId).isPresent()) {
            log.debug("Ignoring duplicate OrderPaid for orderId={}", orderId);
            return;
        }
        Instant now = Instant.now(clock);
        KitchenTicket ticket = KitchenTicket.accept(
                UUID.randomUUID(),
                orderId,
                now,
                Duration.ofMillis(properties.getAcceptDelayMs())
        );
        KitchenTicket saved = repository.save(ticket);
        eventPublisher.ticketAccepted(saved);
        log.info("Kitchen accepted orderId={} ticketId={}", orderId, saved.id());
    }

    @Override
    @Transactional
    public void onOrderCancelled(EventEnvelope<OrderCancelledEvent> event) {
        UUID orderId = event.payload().orderId();
        Optional<KitchenTicket> existing = repository.findByOrderId(orderId);
        if (existing.isEmpty()) {
            log.debug("No kitchen ticket for cancelled orderId={}", orderId);
            return;
        }
        KitchenTicket ticket = existing.get();
        if (!ticket.canCancel()) {
            log.warn("Kitchen ticket {} for orderId={} cannot be cancelled in status {}",
                    ticket.id(), orderId, ticket.status());
            return;
        }
        Instant now = Instant.now(clock);
        ticket.cancel(now);
        KitchenTicket saved = repository.save(ticket);
        eventPublisher.ticketCancelled(saved, event.payload().reason());
        log.info("Kitchen cancelled ticketId={} orderId={}", saved.id(), orderId);
    }

    @Override
    @Transactional
    public void processDueTickets() {
        Instant now = Instant.now(clock);
        List<KitchenTicket> dueTickets = repository.findDue(now, properties.getBatchSize());
        for (KitchenTicket ticket : dueTickets) {
            if (ticket.status() == KitchenTicketStatus.ACCEPTED) {
                ticket.startPreparation(now, Duration.ofMillis(properties.getPreparationDelayMs()));
                KitchenTicket saved = repository.save(ticket);
                eventPublisher.preparationStarted(saved);
                log.info("Preparation started ticketId={} orderId={}", saved.id(), saved.orderId());
            } else if (ticket.status() == KitchenTicketStatus.PREPARING) {
                ticket.markReady(now);
                KitchenTicket saved = repository.save(ticket);
                eventPublisher.orderReady(saved);
                log.info("Order ready ticketId={} orderId={}", saved.id(), saved.orderId());
            }
        }
    }
}
