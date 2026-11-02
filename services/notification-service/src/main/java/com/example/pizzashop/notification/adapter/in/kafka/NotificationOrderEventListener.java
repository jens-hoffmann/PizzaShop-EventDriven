package com.example.pizzashop.notification.adapter.in.kafka;

import com.example.pizzashop.contracts.EventEnvelope;
import com.example.pizzashop.contracts.EventTypes;
import com.example.pizzashop.contracts.OrderStatusChangedEvent;
import com.example.pizzashop.contracts.Topics;
import com.example.pizzashop.messaging.EventEnvelopeParser;
import com.example.pizzashop.messaging.EventMdc;
import com.example.pizzashop.messaging.IdempotentEventHandler;
import com.example.pizzashop.notification.application.port.in.NotificationUseCases;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
class NotificationOrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationOrderEventListener.class);
    private static final String CONSUMER = "notification-service";

    private final EventEnvelopeParser parser;
    private final IdempotentEventHandler handler;
    private final NotificationUseCases useCases;

    NotificationOrderEventListener(
            EventEnvelopeParser parser,
            IdempotentEventHandler handler,
            NotificationUseCases useCases
    ) {
        this.parser = parser;
        this.handler = handler;
        this.useCases = useCases;
    }

    @KafkaListener(topics = Topics.ORDER_EVENTS, groupId = "notification-service")
    public void onOrderEvent(String payload) {
        EventEnvelope<JsonNode> envelope = parser.parse(payload);
        EventMdc.set(envelope);
        try {
            if (EventTypes.ORDER_STATUS_CHANGED.equals(envelope.eventType())) {
                handler.handle(envelope, CONSUMER, OrderStatusChangedEvent.class, useCases::onOrderStatusChanged);
            } else {
                log.debug("Ignoring order event type {} for notifications", envelope.eventType());
            }
        } finally {
            EventMdc.clear();
        }
    }
}
