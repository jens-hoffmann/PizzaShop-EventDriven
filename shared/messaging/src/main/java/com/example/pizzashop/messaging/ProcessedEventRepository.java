package com.example.pizzashop.messaging;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, ProcessedEventId> {

    @Modifying
    @Query(value = """
            INSERT INTO processed_event (event_id, consumer, processed_at)
            VALUES (:eventId, :consumer, :processedAt)
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("eventId") UUID eventId,
            @Param("consumer") String consumer,
            @Param("processedAt") Instant processedAt
    );
}
