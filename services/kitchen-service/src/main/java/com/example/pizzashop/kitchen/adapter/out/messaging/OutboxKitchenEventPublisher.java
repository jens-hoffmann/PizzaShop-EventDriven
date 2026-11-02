package com.example.pizzashop.kitchen.adapter.out.messaging;

import com.example.pizzashop.contracts.EventEnvelope;
import com.example.pizzashop.contracts.EventTypes;
import com.example.pizzashop.contracts.KitchenTicketAcceptedEvent;
import com.example.pizzashop.contracts.KitchenTicketCancelledEvent;
import com.example.pizzashop.contracts.OrderReadyEvent;
import com.example.pizzashop.contracts.PreparationStartedEvent;
import com.example.pizzashop.kitchen.application.port.out.KitchenEventPublisher;
import com.example.pizzashop.kitchen.domain.model.KitchenTicket;
import com.example.pizzashop.messaging.EventMdc;
import com.example.pizzashop.messaging.OutboxWriter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OutboxKitchenEventPublisher implements KitchenEventPublisher {

    private static final String PRODUCER = "kitchen-service";

    private final OutboxWriter outboxWriter;

    public OutboxKitchenEventPublisher(OutboxWriter outboxWriter) {
        this.outboxWriter = outboxWriter;
    }

    @Override
    public void ticketAccepted(KitchenTicket ticket) {
        publish(EventTypes.KITCHEN_TICKET_ACCEPTED, ticket, new KitchenTicketAcceptedEvent(
                ticket.id(),
                ticket.orderId(),
                ticket.acceptedAt()
        ));
    }

    @Override
    public void preparationStarted(KitchenTicket ticket) {
        publish(EventTypes.KITCHEN_PREPARATION_STARTED, ticket, new PreparationStartedEvent(
                ticket.id(),
                ticket.orderId(),
                ticket.preparationStartedAt()
        ));
    }

    @Override
    public void orderReady(KitchenTicket ticket) {
        publish(EventTypes.KITCHEN_ORDER_READY, ticket, new OrderReadyEvent(
                ticket.id(),
                ticket.orderId(),
                ticket.readyAt()
        ));
    }

    @Override
    public void ticketCancelled(KitchenTicket ticket, String reason) {
        publish(EventTypes.KITCHEN_TICKET_CANCELLED, ticket, new KitchenTicketCancelledEvent(
                ticket.id(),
                ticket.orderId(),
                reason,
                ticket.cancelledAt()
        ));
    }

    private void publish(String eventType, KitchenTicket ticket, Object payload) {
        UUID correlationId = EventMdc.correlationId();
        if (correlationId == null) {
            correlationId = UUID.randomUUID();
        }
        outboxWriter.append(EventEnvelope.create(
                eventType,
                PRODUCER,
                correlationId,
                EventMdc.causationId(),
                ticket.orderId().toString(),
                payload
        ));
    }
}
