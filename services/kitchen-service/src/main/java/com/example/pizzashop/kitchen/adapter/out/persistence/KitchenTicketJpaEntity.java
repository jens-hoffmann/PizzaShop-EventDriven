package com.example.pizzashop.kitchen.adapter.out.persistence;

import com.example.pizzashop.kitchen.domain.model.KitchenTicketStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "kitchen_ticket")
class KitchenTicketJpaEntity {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private KitchenTicketStatus status;

    @Column(name = "accepted_at", nullable = false)
    private Instant acceptedAt;

    @Column(name = "preparation_started_at")
    private Instant preparationStartedAt;

    @Column(name = "ready_at")
    private Instant readyAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "next_action_at")
    private Instant nextActionAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected KitchenTicketJpaEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public KitchenTicketStatus getStatus() {
        return status;
    }

    public void setStatus(KitchenTicketStatus status) {
        this.status = status;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Instant getPreparationStartedAt() {
        return preparationStartedAt;
    }

    public void setPreparationStartedAt(Instant preparationStartedAt) {
        this.preparationStartedAt = preparationStartedAt;
    }

    public Instant getReadyAt() {
        return readyAt;
    }

    public void setReadyAt(Instant readyAt) {
        this.readyAt = readyAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public Instant getNextActionAt() {
        return nextActionAt;
    }

    public void setNextActionAt(Instant nextActionAt) {
        this.nextActionAt = nextActionAt;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
