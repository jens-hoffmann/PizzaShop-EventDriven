package com.example.pizzashop.notification.application.port.out;

import com.example.pizzashop.notification.application.port.in.NotificationUseCases.NotificationPageResult;
import com.example.pizzashop.notification.domain.model.Notification;

public interface NotificationRepository {

    Notification save(Notification notification);

    NotificationPageResult findPage(String customerId, int page, int size);
}
