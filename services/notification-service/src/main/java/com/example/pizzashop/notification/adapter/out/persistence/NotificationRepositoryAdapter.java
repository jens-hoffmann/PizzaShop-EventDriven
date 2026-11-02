package com.example.pizzashop.notification.adapter.out.persistence;

import com.example.pizzashop.notification.application.port.in.NotificationUseCases.NotificationPageResult;
import com.example.pizzashop.notification.application.port.out.NotificationRepository;
import com.example.pizzashop.notification.domain.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
class NotificationRepositoryAdapter implements NotificationRepository {

    private final SpringDataNotificationRepository repository;

    NotificationRepositoryAdapter(SpringDataNotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.setId(notification.id());
        entity.setOrderId(notification.orderId());
        entity.setCustomerId(notification.customerId());
        entity.setStatus(notification.status());
        entity.setMessage(notification.message());
        entity.setCreatedAt(notification.createdAt());
        repository.save(entity);
        return notification;
    }

    @Override
    public NotificationPageResult findPage(String customerId, int page, int size) {
        Page<NotificationJpaEntity> result = repository.findByCustomerIdOrderByCreatedAtDesc(
                customerId,
                PageRequest.of(page, size)
        );
        return new NotificationPageResult(
                result.getContent().stream()
                        .map(entity -> new Notification(
                                entity.getId(),
                                entity.getOrderId(),
                                entity.getCustomerId(),
                                entity.getStatus(),
                                entity.getMessage(),
                                entity.getCreatedAt()
                        ))
                        .toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}
