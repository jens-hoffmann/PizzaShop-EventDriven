package com.example.pizzashop.catalog.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataPizzaRepository extends JpaRepository<PizzaJpaEntity, UUID> {

    List<PizzaJpaEntity> findAllByOrderByNameAsc();

    List<PizzaJpaEntity> findByAvailableOrderByNameAsc(boolean available);

    Optional<PizzaJpaEntity> findByNameIgnoreCase(String name);
}
