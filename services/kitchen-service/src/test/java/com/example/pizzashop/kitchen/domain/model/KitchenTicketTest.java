package com.example.pizzashop.kitchen.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KitchenTicketTest {

    private static final Instant NOW = Instant.parse("2026-11-01T12:00:00Z");

    @Test
    void progressesFromAcceptedToReady() {
        KitchenTicket ticket = KitchenTicket.accept(
                UUID.randomUUID(), UUID.randomUUID(), NOW, Duration.ofSeconds(1));

        assertThat(ticket.status()).isEqualTo(KitchenTicketStatus.ACCEPTED);
        assertThat(ticket.nextActionAt()).isEqualTo(NOW.plusSeconds(1));
        assertThat(ticket.isDue(NOW)).isFalse();
        assertThat(ticket.isDue(NOW.plusSeconds(1))).isTrue();

        ticket.startPreparation(NOW.plusSeconds(1), Duration.ofSeconds(4));
        assertThat(ticket.status()).isEqualTo(KitchenTicketStatus.PREPARING);
        assertThat(ticket.nextActionAt()).isEqualTo(NOW.plusSeconds(5));

        ticket.markReady(NOW.plusSeconds(5));
        assertThat(ticket.status()).isEqualTo(KitchenTicketStatus.READY);
        assertThat(ticket.readyAt()).isEqualTo(NOW.plusSeconds(5));
        assertThat(ticket.nextActionAt()).isNull();
    }

    @Test
    void cancelsAnAcceptedTicket() {
        KitchenTicket ticket = KitchenTicket.accept(
                UUID.randomUUID(), UUID.randomUUID(), NOW, Duration.ofSeconds(1));

        ticket.cancel(NOW.plusSeconds(1));

        assertThat(ticket.status()).isEqualTo(KitchenTicketStatus.CANCELLED);
        assertThat(ticket.cancelledAt()).isEqualTo(NOW.plusSeconds(1));
        assertThat(ticket.nextActionAt()).isNull();
    }

    @Test
    void rejectsCancellingAReadyTicket() {
        KitchenTicket ticket = KitchenTicket.accept(
                UUID.randomUUID(), UUID.randomUUID(), NOW, Duration.ofSeconds(1));
        ticket.startPreparation(NOW.plusSeconds(1), Duration.ofSeconds(1));
        ticket.markReady(NOW.plusSeconds(2));

        assertThatThrownBy(() -> ticket.cancel(NOW.plusSeconds(3)))
                .isInstanceOf(InvalidKitchenTicketTransitionException.class);
    }
}
