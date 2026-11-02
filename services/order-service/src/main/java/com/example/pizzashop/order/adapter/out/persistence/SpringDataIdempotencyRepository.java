package com.example.pizzashop.order.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataIdempotencyRepository extends JpaRepository<IdempotencyJpaEntity, String> {
}
