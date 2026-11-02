package com.example.pizzashop.payment.adapter.out.messaging;

import com.example.pizzashop.contracts.EventEnvelope;
import com.example.pizzashop.contracts.EventTypes;
import com.example.pizzashop.contracts.PaymentFailedEvent;
import com.example.pizzashop.contracts.PaymentRefundedEvent;
import com.example.pizzashop.contracts.PaymentSucceededEvent;
import com.example.pizzashop.messaging.EventMdc;
import com.example.pizzashop.messaging.OutboxWriter;
import com.example.pizzashop.payment.application.port.out.PaymentEventPublisher;
import com.example.pizzashop.payment.domain.model.Payment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OutboxPaymentEventPublisher implements PaymentEventPublisher {

    private static final String PRODUCER = "payment-service";

    private final OutboxWriter outboxWriter;

    public OutboxPaymentEventPublisher(OutboxWriter outboxWriter) {
        this.outboxWriter = outboxWriter;
    }

    @Override
    public void paymentSucceeded(Payment payment) {
        publish(EventTypes.PAYMENT_SUCCEEDED, payment, new PaymentSucceededEvent(
                payment.id(),
                payment.orderId(),
                payment.amount(),
                payment.currency(),
                payment.updatedAt()
        ));
    }

    @Override
    public void paymentFailed(Payment payment) {
        publish(EventTypes.PAYMENT_FAILED, payment, new PaymentFailedEvent(
                payment.id(),
                payment.orderId(),
                payment.failureReason(),
                payment.updatedAt()
        ));
    }

    @Override
    public void paymentRefunded(Payment payment) {
        publish(EventTypes.PAYMENT_REFUNDED, payment, new PaymentRefundedEvent(
                payment.id(),
                payment.orderId(),
                payment.amount(),
                payment.updatedAt(),
                "Order cancelled"
        ));
    }

    private void publish(String eventType, Payment payment, Object payload) {
        UUID correlationId = EventMdc.correlationId();
        if (correlationId == null) {
            correlationId = UUID.randomUUID();
        }
        outboxWriter.append(EventEnvelope.create(
                eventType,
                PRODUCER,
                correlationId,
                EventMdc.causationId(),
                payment.orderId().toString(),
                payload
        ));
    }
}
