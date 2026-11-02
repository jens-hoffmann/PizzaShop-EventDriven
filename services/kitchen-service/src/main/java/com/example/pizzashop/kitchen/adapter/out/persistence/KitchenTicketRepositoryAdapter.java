package com.example.pizzashop.kitchen.adapter.out.persistence;

import com.example.pizzashop.kitchen.application.port.out.KitchenTicketRepository;
import com.example.pizzashop.kitchen.domain.model.KitchenTicket;
import com.example.pizzashop.kitchen.domain.model.KitchenTicketStatus;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class KitchenTicketRepositoryAdapter implements KitchenTicketRepository {

    private final SpringDataKitchenTicketRepository repository;

    KitchenTicketRepositoryAdapter(SpringDataKitchenTicketRepository repository) {
        this.repository = repository;
    }

    @Override
    public KitchenTicket save(KitchenTicket ticket) {
        return toDomain(repository.save(toEntity(ticket)));
    }

    @Override
    public Optional<KitchenTicket> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<KitchenTicket> findByOrderId(UUID orderId) {
        return repository.findByOrderId(orderId).map(this::toDomain);
    }

    @Override
    public List<KitchenTicket> findAll(Optional<KitchenTicketStatus> status) {
        List<KitchenTicketJpaEntity> entities = status
                .map(repository::findByStatusOrderByAcceptedAtAsc)
                .orElseGet(repository::findAllByOrderByAcceptedAtAsc);
        return entities.stream().map(this::toDomain).toList();
    }

    @Override
    public List<KitchenTicket> findDue(Instant now, int limit) {
        return repository.findDue(now, limit).stream().map(this::toDomain).toList();
    }

    private KitchenTicket toDomain(KitchenTicketJpaEntity entity) {
        return KitchenTicket.reconstitute(
                entity.getId(),
                entity.getOrderId(),
                entity.getStatus(),
                entity.getAcceptedAt(),
                entity.getPreparationStartedAt(),
                entity.getReadyAt(),
                entity.getCancelledAt(),
                entity.getNextActionAt(),
                entity.getVersion()
        );
    }

    private KitchenTicketJpaEntity toEntity(KitchenTicket ticket) {
        KitchenTicketJpaEntity entity = new KitchenTicketJpaEntity();
        entity.setId(ticket.id());
        entity.setOrderId(ticket.orderId());
        entity.setStatus(ticket.status());
        entity.setAcceptedAt(ticket.acceptedAt());
        entity.setPreparationStartedAt(ticket.preparationStartedAt());
        entity.setReadyAt(ticket.readyAt());
        entity.setCancelledAt(ticket.cancelledAt());
        entity.setNextActionAt(ticket.nextActionAt());
        entity.setVersion(ticket.version());
        return entity;
    }
}
