package com.example.pizzashop.order.application.port.in;

import com.example.pizzashop.contracts.EventEnvelope;
import com.example.pizzashop.contracts.KitchenTicketAcceptedEvent;
import com.example.pizzashop.contracts.KitchenTicketCancelledEvent;
import com.example.pizzashop.contracts.OrderReadyEvent;
import com.example.pizzashop.contracts.PaymentFailedEvent;
import com.example.pizzashop.contracts.PaymentRefundedEvent;
import com.example.pizzashop.contracts.PaymentSucceededEvent;
import com.example.pizzashop.contracts.PreparationStartedEvent;
import com.example.pizzashop.order.domain.model.Order;
import com.example.pizzashop.order.domain.model.OrderStatus;
import com.example.pizzashop.order.domain.model.OrderStatusHistoryEntry;

import java.util.List;
import java.util.UUID;

public interface OrderUseCases {

    PlaceOrderResult place(CreateOrderCommand command, String idempotencyKey);

    Order get(UUID id);

    OrderPageResult list(String customerId, OrderStatus status, int page, int size);

    List<OrderStatusHistoryEntry> history(UUID id);

    Order cancel(UUID id, String reason);

    Order complete(UUID id);

    void onPaymentSucceeded(EventEnvelope<PaymentSucceededEvent> event);

    void onPaymentFailed(EventEnvelope<PaymentFailedEvent> event);

    void onPaymentRefunded(EventEnvelope<PaymentRefundedEvent> event);

    void onKitchenTicketAccepted(EventEnvelope<KitchenTicketAcceptedEvent> event);

    void onPreparationStarted(EventEnvelope<PreparationStartedEvent> event);

    void onOrderReady(EventEnvelope<OrderReadyEvent> event);

    void onKitchenTicketCancelled(EventEnvelope<KitchenTicketCancelledEvent> event);

    record PlaceOrderResult(Order order, boolean replayed) {
    }

    record OrderPageResult(
            List<Order> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }

    record CreateOrderCommand(
            String customerId,
            List<OrderItemCommand> items,
            DeliveryAddressCommand deliveryAddress
    ) {
    }

    record OrderItemCommand(UUID pizzaId, String size, int quantity) {
    }

    record DeliveryAddressCommand(String street, String city, String postalCode) {
    }
}
