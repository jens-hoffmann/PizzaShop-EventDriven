package com.example.pizzashop.order.adapter.out.persistence;

import com.example.pizzashop.order.domain.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, UUID> {

    @Query("""
            SELECT o FROM OrderJpaEntity o
            WHERE (:customerId IS NULL OR o.customerId = :customerId)
              AND (:status IS NULL OR o.status = :status)
            """)
    Page<OrderJpaEntity> findPage(
            @Param("customerId") String customerId,
            @Param("status") OrderStatus status,
            Pageable pageable
    );
}
