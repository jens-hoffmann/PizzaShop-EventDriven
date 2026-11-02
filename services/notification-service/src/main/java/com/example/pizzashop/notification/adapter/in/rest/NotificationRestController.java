package com.example.pizzashop.notification.adapter.in.rest;

import com.example.pizzashop.notification.adapter.in.rest.generated.api.NotificationsApi;
import com.example.pizzashop.notification.adapter.in.rest.generated.model.NotificationPage;
import com.example.pizzashop.notification.adapter.in.rest.generated.model.NotificationResponse;
import com.example.pizzashop.notification.application.port.in.NotificationUseCases;
import com.example.pizzashop.notification.application.port.in.NotificationUseCases.NotificationPageResult;
import com.example.pizzashop.notification.domain.model.Notification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneOffset;

@RestController
class NotificationRestController implements NotificationsApi {

    private final NotificationUseCases useCases;

    NotificationRestController(NotificationUseCases useCases) {
        this.useCases = useCases;
    }

    @Override
    public ResponseEntity<NotificationPage> listNotifications(String customerId, Integer page, Integer size) {
        NotificationPageResult result = useCases.list(
                customerId,
                page == null ? 0 : page,
                size == null ? 20 : size
        );
        NotificationPage response = new NotificationPage(
                result.content().stream().map(this::toResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
        return ResponseEntity.ok(response);
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.id(),
                notification.orderId(),
                notification.customerId(),
                notification.status(),
                notification.message(),
                notification.createdAt().atOffset(ZoneOffset.UTC)
        );
    }
}
