package com.example.pizzashop.messaging;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_message")
public class OutboxMessage {

    @Id
    private UUID id;

    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "event_version", nullable = false)
    private int eventVersion;

    @Column(name = "payload", nullable = false, columnDefinition = "text")
    private String payload;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    protected OutboxMessage() {
    }

    public OutboxMessage(
            UUID id,
            String aggregateId,
            String eventType,
            int eventVersion,
            String payload,
            Instant occurredAt,
            Instant createdAt
    ) {
        this.id = id;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.eventVersion = eventVersion;
        this.payload = payload;
        this.occurredAt = occurredAt;
        this.createdAt = createdAt;
    }

    public void markPublished(Instant publishedAt) {
        this.publishedAt = publishedAt;
        this.nextAttemptAt = null;
        this.lastError = null;
    }

    public void markFailed(String error, Instant now, OutboxProperties properties) {
        this.attempts++;
        this.lastError = error;
        if (attempts >= properties.getMaxAttempts()) {
            this.nextAttemptAt = Instant.parse("9999-12-31T23:59:59Z");
            return;
        }
        long delay = Math.min(
                properties.getInitialBackoffMs() * (1L << Math.min(attempts - 1, 20)),
                properties.getMaxBackoffMs()
        );
        this.nextAttemptAt = now.plusMillis(delay);
    }

    public UUID getId() {
        return id;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public int getEventVersion() {
        return eventVersion;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public String getLastError() {
        return lastError;
    }
}
