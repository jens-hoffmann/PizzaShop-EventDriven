package com.example.pizzashop.payment.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    private static final Instant NOW = Instant.parse("2026-11-01T12:00:00Z");

    @Test
    void succeedsThenRefunds() {
        Payment payment = Payment.create(
                UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("23.00"), "EUR", NOW);

        assertThat(payment.status()).isEqualTo(PaymentStatus.PENDING);

        payment.markSucceeded(NOW.plusSeconds(1));
        assertThat(payment.status()).isEqualTo(PaymentStatus.SUCCEEDED);

        payment.refund(NOW.plusSeconds(2));
        assertThat(payment.status()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void failsWithAReason() {
        Payment payment = Payment.create(
                UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("23.00"), "EUR", NOW);

        payment.markFailed("Card declined", NOW.plusSeconds(1));

        assertThat(payment.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.failureReason()).isEqualTo("Card declined");
    }

    @Test
    void rejectsRefundingAnUnpaidPayment() {
        Payment payment = Payment.create(
                UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("23.00"), "EUR", NOW);

        assertThatThrownBy(() -> payment.refund(NOW.plusSeconds(1)))
                .isInstanceOf(InvalidPaymentTransitionException.class);
    }

    @Test
    void rejectsANonPositiveAmount() {
        assertThatThrownBy(() -> Payment.create(
                UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ZERO, "EUR", NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
