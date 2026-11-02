package com.example.pizzashop.payment.application.service;

import com.example.pizzashop.contracts.EventEnvelope;
import com.example.pizzashop.contracts.OrderCancelledEvent;
import com.example.pizzashop.contracts.OrderPlacedEvent;
import com.example.pizzashop.payment.application.port.in.PaymentUseCases;
import com.example.pizzashop.payment.application.port.out.PaymentEventPublisher;
import com.example.pizzashop.payment.application.port.out.PaymentRepository;
import com.example.pizzashop.payment.config.PaymentMode;
import com.example.pizzashop.payment.config.PaymentProperties;
import com.example.pizzashop.payment.domain.model.Payment;
import com.example.pizzashop.payment.domain.model.PaymentStatus;
import com.example.pizzashop.web.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentService implements PaymentUseCases {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository repository;
    private final PaymentEventPublisher eventPublisher;
    private final PaymentProperties properties;
    private final Clock clock;

    public PaymentService(
            PaymentRepository repository,
            PaymentEventPublisher eventPublisher,
            PaymentProperties properties,
            Clock clock
    ) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getByOrderId(UUID orderId) {
        return repository.findByOrderId(orderId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", "No payment for order " + orderId));
    }

    @Override
    @Transactional
    public void onOrderPlaced(EventEnvelope<OrderPlacedEvent> event) {
        UUID orderId = event.payload().orderId();
        if (repository.findByOrderId(orderId).isPresent()) {
            log.debug("Ignoring duplicate OrderPlaced for orderId={}", orderId);
            return;
        }

        Instant now = Instant.now(clock);
        Payment payment = repository.save(Payment.create(
                UUID.randomUUID(),
                orderId,
                event.payload().total(),
                event.payload().currency(),
                now
        ));

        sleep(properties.getDelayMs());

        if (shouldSucceed()) {
            payment.markSucceeded(Instant.now(clock));
            Payment saved = repository.save(payment);
            eventPublisher.paymentSucceeded(saved);
            log.info("Payment succeeded paymentId={} orderId={}", saved.id(), orderId);
        } else {
            payment.markFailed("Simulated payment failure", Instant.now(clock));
            Payment saved = repository.save(payment);
            eventPublisher.paymentFailed(saved);
            log.info("Payment failed paymentId={} orderId={}", saved.id(), orderId);
        }
    }

    @Override
    @Transactional
    public void onOrderCancelled(EventEnvelope<OrderCancelledEvent> event) {
        UUID orderId = event.payload().orderId();
        Optional<Payment> existing = repository.findByOrderId(orderId);
        if (existing.isEmpty()) {
            log.debug("No payment for cancelled orderId={}", orderId);
            return;
        }
        Payment payment = existing.get();
        Instant now = Instant.now(clock);
        if (payment.status() == PaymentStatus.SUCCEEDED) {
            payment.refund(now);
            Payment saved = repository.save(payment);
            eventPublisher.paymentRefunded(saved);
            log.info("Payment refunded paymentId={} orderId={}", saved.id(), orderId);
        } else if (payment.status() == PaymentStatus.PENDING) {
            payment.markFailed("Order cancelled before payment", now);
            Payment saved = repository.save(payment);
            eventPublisher.paymentFailed(saved);
            log.info("Pending payment failed after cancellation paymentId={} orderId={}", saved.id(), orderId);
        }
    }

    private boolean shouldSucceed() {
        return switch (properties.getMode()) {
            case SUCCESS -> true;
            case FAILURE -> false;
            case RANDOM -> ThreadLocalRandom.current().nextDouble() >= properties.getFailureRate();
        };
    }

    private static void sleep(long delayMs) {
        if (delayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Payment simulation interrupted", exception);
        }
    }
}
