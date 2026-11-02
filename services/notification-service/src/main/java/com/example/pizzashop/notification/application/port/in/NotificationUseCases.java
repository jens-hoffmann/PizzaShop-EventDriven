package com.example.pizzashop.notification.application.port.in;

import com.example.pizzashop.contracts.EventEnvelope;
import com.example.pizzashop.contracts.OrderStatusChangedEvent;
import com.example.pizzashop.notification.domain.model.Notification;

import java.util.List;

public interface NotificationUseCases {

    NotificationPageResult list(String customerId, int page, int size);

    void onOrderStatusChanged(EventEnvelope<OrderStatusChangedEvent> event);

    record NotificationPageResult(
            List<Notification> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }
}
