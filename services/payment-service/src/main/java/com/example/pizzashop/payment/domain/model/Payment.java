package com.example.pizzashop.payment.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class Payment {

    private final UUID id;
    private final UUID orderId;
    private final BigDecimal amount;
    private final String currency;
    private PaymentStatus status;
    private String failureReason;
    private final Instant createdAt;
    private Instant updatedAt;
    private long version;

    private Payment(
            UUID id,
            UUID orderId,
            BigDecimal amount,
            String currency,
            PaymentStatus status,
            String failureReason,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public static Payment create(
            UUID id,
            UUID orderId,
            BigDecimal amount,
            String currency,
            Instant now
    ) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        return new Payment(
                id,
                orderId,
                amount,
                currency,
                PaymentStatus.PENDING,
                null,
                now,
                now,
                0
        );
    }

    public static Payment reconstitute(
            UUID id,
            UUID orderId,
            BigDecimal amount,
            String currency,
            PaymentStatus status,
            String failureReason,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        return new Payment(
                id,
                orderId,
                amount,
                currency,
                status,
                failureReason,
                createdAt,
                updatedAt,
                version
        );
    }

    public void markSucceeded(Instant now) {
        if (status != PaymentStatus.PENDING) {
            throw new InvalidPaymentTransitionException(status, "succeed");
        }
        status = PaymentStatus.SUCCEEDED;
        updatedAt = now;
    }

    public void markFailed(String reason, Instant now) {
        if (status != PaymentStatus.PENDING) {
            throw new InvalidPaymentTransitionException(status, "fail");
        }
        status = PaymentStatus.FAILED;
        failureReason = reason;
        updatedAt = now;
    }

    public void refund(Instant now) {
        if (status == PaymentStatus.REFUNDED) {
            return;
        }
        if (status != PaymentStatus.SUCCEEDED) {
            throw new InvalidPaymentTransitionException(status, "refund");
        }
        status = PaymentStatus.REFUNDED;
        updatedAt = now;
    }

    public UUID id() {
        return id;
    }

    public UUID orderId() {
        return orderId;
    }

    public BigDecimal amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }

    public PaymentStatus status() {
        return status;
    }

    public String failureReason() {
        return failureReason;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public long version() {
        return version;
    }
}
