package com.example.pizzashop.notification.application.service;

import com.example.pizzashop.contracts.EventEnvelope;
import com.example.pizzashop.contracts.OrderStatusChangedEvent;
import com.example.pizzashop.notification.application.port.in.NotificationUseCases;
import com.example.pizzashop.notification.application.port.out.NotificationRepository;
import com.example.pizzashop.notification.domain.model.Notification;
import com.example.pizzashop.web.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class NotificationService implements NotificationUseCases {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository repository;
    private final Clock clock;

    public NotificationService(NotificationRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPageResult list(String customerId, int page, int size) {
        if (customerId == null || customerId.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CUSTOMER_ID", "customerId is required");
        }
        if (page < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGE", "page must be zero or greater");
        }
        if (size < 1 || size > 100) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_SIZE", "size must be between 1 and 100");
        }
        return repository.findPage(customerId, page, size);
    }

    @Override
    @Transactional
    public void onOrderStatusChanged(EventEnvelope<OrderStatusChangedEvent> event) {
        OrderStatusChangedEvent payload = event.payload();
        Notification notification = new Notification(
                UUID.randomUUID(),
                payload.orderId(),
                payload.customerId(),
                payload.newStatus(),
                message(payload),
                Instant.now(clock)
        );
        repository.save(notification);
        log.info("Simulated notification orderId={} customerId={} status={} message={}",
                payload.orderId(), payload.customerId(), payload.newStatus(), notification.message());
    }

    private String message(OrderStatusChangedEvent payload) {
        String base = switch (payload.newStatus()) {
            case "PLACED" -> "Your order has been received.";
            case "PAID" -> "Your payment was successful.";
            case "CONFIRMED" -> "Our kitchen accepted your order.";
            case "PREPARING" -> "Your pizza is being prepared.";
            case "READY" -> "Your order is ready.";
            case "COMPLETED" -> "Your order is completed. Enjoy your meal!";
            case "CANCELLED" -> "Your order was cancelled.";
            case "PAYMENT_FAILED" -> "Your payment failed.";
            default -> "Your order status changed to " + payload.newStatus() + ".";
        };
        if (payload.reason() == null || payload.reason().isBlank()) {
            return base;
        }
        if ("CANCELLED".equals(payload.newStatus()) || "PAYMENT_FAILED".equals(payload.newStatus())) {
            return base + " Reason: " + payload.reason();
        }
        return base;
    }
}
