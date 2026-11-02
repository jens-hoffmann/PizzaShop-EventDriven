package com.example.pizzashop.notification.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataNotificationRepository extends JpaRepository<NotificationJpaEntity, UUID> {

    Page<NotificationJpaEntity> findByCustomerIdOrderByCreatedAtDesc(String customerId, Pageable pageable);
}
