package com.example.pizzashop.payment.adapter.out.persistence;

import com.example.pizzashop.payment.application.port.out.PaymentRepository;
import com.example.pizzashop.payment.domain.model.Payment;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
class PaymentRepositoryAdapter implements PaymentRepository {

    private final SpringDataPaymentRepository repository;

    PaymentRepositoryAdapter(SpringDataPaymentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Payment save(Payment payment) {
        return toDomain(repository.save(toEntity(payment)));
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return repository.findByOrderId(orderId).map(this::toDomain);
    }

    private Payment toDomain(PaymentJpaEntity entity) {
        return Payment.reconstitute(
                entity.getId(),
                entity.getOrderId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getFailureReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    private PaymentJpaEntity toEntity(Payment payment) {
        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.setId(payment.id());
        entity.setOrderId(payment.orderId());
        entity.setAmount(payment.amount());
        entity.setCurrency(payment.currency());
        entity.setStatus(payment.status());
        entity.setFailureReason(payment.failureReason());
        entity.setCreatedAt(payment.createdAt());
        entity.setUpdatedAt(payment.updatedAt());
        entity.setVersion(payment.version());
        return entity;
    }
}
