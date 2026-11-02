package com.example.pizzashop.kitchen.adapter.out.persistence;

import com.example.pizzashop.kitchen.domain.model.KitchenTicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataKitchenTicketRepository extends JpaRepository<KitchenTicketJpaEntity, UUID> {

    Optional<KitchenTicketJpaEntity> findByOrderId(UUID orderId);

    List<KitchenTicketJpaEntity> findAllByOrderByAcceptedAtAsc();

    List<KitchenTicketJpaEntity> findByStatusOrderByAcceptedAtAsc(KitchenTicketStatus status);

    @Query(value = """
            SELECT * FROM kitchen_ticket
            WHERE status IN ('ACCEPTED', 'PREPARING')
              AND next_action_at IS NOT NULL
              AND next_action_at <= :now
            ORDER BY next_action_at
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<KitchenTicketJpaEntity> findDue(@Param("now") Instant now, @Param("limit") int limit);
}
