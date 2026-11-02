package com.example.pizzashop.order.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface IdempotencyStore {

    Optional<IdempotencyRecord> findByKey(String key);

    void save(IdempotencyRecord record);

    void delete(String key);

    record IdempotencyRecord(
            String key,
            String requestHash,
            UUID orderId,
            Instant createdAt,
            Instant expiresAt
    ) {
    }
}
