package com.example.pizzashop.order.adapter.in.kafka;

import com.example.pizzashop.contracts.EventEnvelope;
import com.example.pizzashop.contracts.EventTypes;
import com.example.pizzashop.contracts.PaymentFailedEvent;
import com.example.pizzashop.contracts.PaymentRefundedEvent;
import com.example.pizzashop.contracts.PaymentSucceededEvent;
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
class OrderPaymentEventListener {

    private static final String CONSUMER = "order-service.payment";

    private final EventEnvelopeParser parser;
    private final IdempotentEventHandler handler;
    private final OrderUseCases useCases;

    OrderPaymentEventListener(
            EventEnvelopeParser parser,
            IdempotentEventHandler handler,
            OrderUseCases useCases
    ) {
        this.parser = parser;
        this.handler = handler;
        this.useCases = useCases;
    }

    @KafkaListener(topics = Topics.PAYMENT_EVENTS, groupId = "order-service-payment")
    public void onPaymentEvent(String payload) {
        EventEnvelope<JsonNode> envelope = parser.parse(payload);
        EventMdc.set(envelope);
        try {
            switch (envelope.eventType()) {
                case EventTypes.PAYMENT_SUCCEEDED -> handler.handle(
                        envelope, CONSUMER, PaymentSucceededEvent.class, useCases::onPaymentSucceeded);
                case EventTypes.PAYMENT_FAILED -> handler.handle(
                        envelope, CONSUMER, PaymentFailedEvent.class, useCases::onPaymentFailed);
                case EventTypes.PAYMENT_REFUNDED -> handler.handle(
                        envelope, CONSUMER, PaymentRefundedEvent.class, useCases::onPaymentRefunded);
                default -> throw new InvalidEventException(
                        "Unexpected payment event type: " + envelope.eventType());
            }
        } finally {
            EventMdc.clear();
        }
    }
}
