package com.example.pizzashop.kitchen.adapter.in.kafka;

import com.example.pizzashop.contracts.EventEnvelope;
import com.example.pizzashop.contracts.EventTypes;
import com.example.pizzashop.contracts.OrderCancelledEvent;
import com.example.pizzashop.contracts.OrderPaidEvent;
import com.example.pizzashop.contracts.Topics;
import com.example.pizzashop.kitchen.application.port.in.KitchenUseCases;
import com.example.pizzashop.messaging.EventEnvelopeParser;
import com.example.pizzashop.messaging.EventMdc;
import com.example.pizzashop.messaging.IdempotentEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
class KitchenOrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(KitchenOrderEventListener.class);
    private static final String CONSUMER = "kitchen-service";

    private final EventEnvelopeParser parser;
    private final IdempotentEventHandler handler;
    private final KitchenUseCases useCases;

    KitchenOrderEventListener(
            EventEnvelopeParser parser,
            IdempotentEventHandler handler,
            KitchenUseCases useCases
    ) {
        this.parser = parser;
        this.handler = handler;
        this.useCases = useCases;
    }

    @KafkaListener(topics = Topics.ORDER_EVENTS, groupId = "kitchen-service")
    public void onOrderEvent(String payload) {
        EventEnvelope<JsonNode> envelope = parser.parse(payload);
        EventMdc.set(envelope);
        try {
            switch (envelope.eventType()) {
                case EventTypes.ORDER_PAID -> handler.handle(
                        envelope, CONSUMER, OrderPaidEvent.class, useCases::onOrderPaid);
                case EventTypes.ORDER_CANCELLED -> handler.handle(
                        envelope, CONSUMER, OrderCancelledEvent.class, useCases::onOrderCancelled);
                default -> log.debug("Ignoring order event type {} for kitchen", envelope.eventType());
            }
        } finally {
            EventMdc.clear();
        }
    }
}
