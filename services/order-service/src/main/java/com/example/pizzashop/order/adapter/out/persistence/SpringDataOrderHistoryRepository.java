package com.example.pizzashop.order.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataOrderHistoryRepository extends JpaRepository<OrderStatusHistoryJpaEntity, UUID> {

    List<OrderStatusHistoryJpaEntity> findByOrderIdOrderByOccurredAtAsc(UUID orderId);
}
