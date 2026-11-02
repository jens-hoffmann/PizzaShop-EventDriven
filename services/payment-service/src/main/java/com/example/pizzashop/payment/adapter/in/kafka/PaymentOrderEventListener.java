package com.example.pizzashop.payment.adapter.in.kafka;

import com.example.pizzashop.contracts.EventEnvelope;
import com.example.pizzashop.contracts.EventTypes;
import com.example.pizzashop.contracts.OrderCancelledEvent;
import com.example.pizzashop.contracts.OrderPlacedEvent;
import com.example.pizzashop.contracts.Topics;
import com.example.pizzashop.messaging.EventEnvelopeParser;
import com.example.pizzashop.messaging.EventMdc;
import com.example.pizzashop.messaging.IdempotentEventHandler;
import com.example.pizzashop.payment.application.port.in.PaymentUseCases;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
class PaymentOrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentOrderEventListener.class);
    private static final String CONSUMER = "payment-service";

    private final EventEnvelopeParser parser;
    private final IdempotentEventHandler handler;
    private final PaymentUseCases useCases;

    PaymentOrderEventListener(
            EventEnvelopeParser parser,
            IdempotentEventHandler handler,
            PaymentUseCases useCases
    ) {
        this.parser = parser;
        this.handler = handler;
        this.useCases = useCases;
    }

    @KafkaListener(topics = Topics.ORDER_EVENTS, groupId = "payment-service")
    public void onOrderEvent(String payload) {
        EventEnvelope<JsonNode> envelope = parser.parse(payload);
        EventMdc.set(envelope);
        try {
            switch (envelope.eventType()) {
                case EventTypes.ORDER_PLACED -> handler.handle(
                        envelope, CONSUMER, OrderPlacedEvent.class, useCases::onOrderPlaced);
                case EventTypes.ORDER_CANCELLED -> handler.handle(
                        envelope, CONSUMER, OrderCancelledEvent.class, useCases::onOrderCancelled);
                default -> log.debug("Ignoring order event type {} for payment", envelope.eventType());
            }
        } finally {
            EventMdc.clear();
        }
    }
}
