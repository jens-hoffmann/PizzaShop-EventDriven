package com.example.pizzashop.order.adapter.out.persistence;

import com.example.pizzashop.order.application.port.out.OrderHistoryRepository;
import com.example.pizzashop.order.domain.model.OrderStatusHistoryEntry;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
class OrderHistoryRepositoryAdapter implements OrderHistoryRepository {

    private final SpringDataOrderHistoryRepository repository;

    OrderHistoryRepositoryAdapter(SpringDataOrderHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public void append(OrderStatusHistoryEntry entry) {
        OrderStatusHistoryJpaEntity entity = new OrderStatusHistoryJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setOrderId(entry.orderId());
        entity.setStatus(entry.status());
        entity.setReason(entry.reason());
        entity.setOccurredAt(entry.occurredAt());
        entity.setSourceEventId(entry.sourceEventId());
        repository.save(entity);
    }

    @Override
    public List<OrderStatusHistoryEntry> findByOrderId(UUID orderId) {
        return repository.findByOrderIdOrderByOccurredAtAsc(orderId).stream()
                .map(entity -> new OrderStatusHistoryEntry(
                        entity.getOrderId(),
                        entity.getStatus(),
                        entity.getReason(),
                        entity.getOccurredAt(),
                        entity.getSourceEventId()
                ))
                .toList();
    }
}
