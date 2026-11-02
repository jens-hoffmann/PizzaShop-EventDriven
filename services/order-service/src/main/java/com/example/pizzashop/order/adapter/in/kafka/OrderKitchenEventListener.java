package com.example.pizzashop.order.adapter.in.kafka;

import com.example.pizzashop.contracts.EventEnvelope;
import com.example.pizzashop.contracts.EventTypes;
import com.example.pizzashop.contracts.KitchenTicketAcceptedEvent;
import com.example.pizzashop.contracts.KitchenTicketCancelledEvent;
import com.example.pizzashop.contracts.OrderReadyEvent;
import com.example.pizzashop.contracts.PreparationStartedEvent;
import com.example.pizzashop.contracts.Topics;
import com.example.pizzashop.messaging.EventEnvelopeParser;
import com.example.pizzashop.messaging.EventMdc;
import com.example.pizzashop.messaging.IdempotentEventHandler;
import com.example.pizzashop.messaging.InvalidEventException;
import com.example.pizzashop.order.application.port.in.OrderUseCases;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
class OrderKitchenEventListener {

    private static final String CONSUMER = "order-service.kitchen";

    private final EventEnvelopeParser parser;
    private final IdempotentEventHandler handler;
    private final OrderUseCases useCases;

    OrderKitchenEventListener(
            EventEnvelopeParser parser,
            IdempotentEventHandler handler,
            OrderUseCases useCases
    ) {
        this.parser = parser;
        this.handler = handler;
        this.useCases = useCases;
    }

    @KafkaListener(topics = Topics.KITCHEN_EVENTS, groupId = "order-service-kitchen")
    public void onKitchenEvent(String payload) {
        EventEnvelope<JsonNode> envelope = parser.parse(payload);
        EventMdc.set(envelope);
        try {
            switch (envelope.eventType()) {
                case EventTypes.KITCHEN_TICKET_ACCEPTED -> handler.handle(
                        envelope, CONSUMER, KitchenTicketAcceptedEvent.class, useCases::onKitchenTicketAccepted);
                case EventTypes.KITCHEN_PREPARATION_STARTED -> handler.handle(
                        envelope, CONSUMER, PreparationStartedEvent.class, useCases::onPreparationStarted);
                case EventTypes.KITCHEN_ORDER_READY -> handler.handle(
                        envelope, CONSUMER, OrderReadyEvent.class, useCases::onOrderReady);
                case EventTypes.KITCHEN_TICKET_CANCELLED -> handler.handle(
                        envelope, CONSUMER, KitchenTicketCancelledEvent.class, useCases::onKitchenTicketCancelled);
                default -> throw new InvalidEventException(
                        "Unexpected kitchen event type: " + envelope.eventType());
            }
        } finally {
            EventMdc.clear();
        }
    }
}
