package com.example.pizzashop.kitchen.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class KitchenTicket {

    private final UUID id;
    private final UUID orderId;
    private KitchenTicketStatus status;
    private final Instant acceptedAt;
    private Instant preparationStartedAt;
    private Instant readyAt;
    private Instant cancelledAt;
    private Instant nextActionAt;
    private long version;

    private KitchenTicket(
            UUID id,
            UUID orderId,
            KitchenTicketStatus status,
            Instant acceptedAt,
            Instant preparationStartedAt,
            Instant readyAt,
            Instant cancelledAt,
            Instant nextActionAt,
            long version
    ) {
        this.id = id;
        this.orderId = orderId;
        this.status = status;
        this.acceptedAt = acceptedAt;
        this.preparationStartedAt = preparationStartedAt;
        this.readyAt = readyAt;
        this.cancelledAt = cancelledAt;
        this.nextActionAt = nextActionAt;
        this.version = version;
    }

    public static KitchenTicket accept(
            UUID id,
            UUID orderId,
            Instant now,
            Duration acceptDelay
    ) {
        return new KitchenTicket(
                id,
                orderId,
                KitchenTicketStatus.ACCEPTED,
                now,
                null,
                null,
                null,
                now.plus(acceptDelay),
                0
        );
    }

    public static KitchenTicket reconstitute(
            UUID id,
            UUID orderId,
            KitchenTicketStatus status,
            Instant acceptedAt,
            Instant preparationStartedAt,
            Instant readyAt,
            Instant cancelledAt,
            Instant nextActionAt,
            long version
    ) {
        return new KitchenTicket(
                id,
                orderId,
                status,
                acceptedAt,
                preparationStartedAt,
                readyAt,
                cancelledAt,
                nextActionAt,
                version
        );
    }

    public void startPreparation(Instant now, Duration preparationDelay) {
        if (status != KitchenTicketStatus.ACCEPTED) {
            throw new InvalidKitchenTicketTransitionException(status, "start preparation for");
        }
        status = KitchenTicketStatus.PREPARING;
        preparationStartedAt = now;
        nextActionAt = now.plus(preparationDelay);
    }

    public void markReady(Instant now) {
        if (status != KitchenTicketStatus.PREPARING) {
            throw new InvalidKitchenTicketTransitionException(status, "finish");
        }
        status = KitchenTicketStatus.READY;
        readyAt = now;
        nextActionAt = null;
    }

    public void cancel(Instant now) {
        if (status == KitchenTicketStatus.READY || status == KitchenTicketStatus.CANCELLED) {
            throw new InvalidKitchenTicketTransitionException(status, "cancel");
        }
        status = KitchenTicketStatus.CANCELLED;
        cancelledAt = now;
        nextActionAt = null;
    }

    public boolean canCancel() {
        return status == KitchenTicketStatus.ACCEPTED || status == KitchenTicketStatus.PREPARING;
    }

    public boolean isDue(Instant now) {
        return nextActionAt != null
                && !nextActionAt.isAfter(now)
                && (status == KitchenTicketStatus.ACCEPTED || status == KitchenTicketStatus.PREPARING);
    }

    public UUID id() {
        return id;
    }

    public UUID orderId() {
        return orderId;
    }

    public KitchenTicketStatus status() {
        return status;
    }

    public Instant acceptedAt() {
        return acceptedAt;
    }

    public Instant preparationStartedAt() {
        return preparationStartedAt;
    }

    public Instant readyAt() {
        return readyAt;
    }

    public Instant cancelledAt() {
        return cancelledAt;
    }

    public Instant nextActionAt() {
        return nextActionAt;
    }

    public long version() {
        return version;
    }
}
