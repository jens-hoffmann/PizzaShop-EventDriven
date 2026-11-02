package com.example.pizzashop.order.adapter.out.persistence;

import com.example.pizzashop.order.application.port.out.IdempotencyStore;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class IdempotencyStoreAdapter implements IdempotencyStore {

    private final SpringDataIdempotencyRepository repository;

    IdempotencyStoreAdapter(SpringDataIdempotencyRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<IdempotencyRecord> findByKey(String key) {
        return repository.findById(key).map(entity -> new IdempotencyRecord(
                entity.getKey(),
                entity.getRequestHash(),
                entity.getOrderId(),
                entity.getCreatedAt(),
                entity.getExpiresAt()
        ));
    }

    @Override
    public void save(IdempotencyRecord record) {
        IdempotencyJpaEntity entity = new IdempotencyJpaEntity();
        entity.setKey(record.key());
        entity.setRequestHash(record.requestHash());
        entity.setOrderId(record.orderId());
        entity.setCreatedAt(record.createdAt());
        entity.setExpiresAt(record.expiresAt());
        repository.save(entity);
    }

    @Override
    public void delete(String key) {
        repository.deleteById(key);
    }
}
