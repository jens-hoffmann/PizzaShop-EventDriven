package com.example.pizzashop.order.application.port.out;

import com.example.pizzashop.order.domain.model.Order;
import com.example.pizzashop.order.domain.model.OrderStatus;

import java.util.UUID;

public interface OrderEventPublisher {

    void orderPlaced(Order order);

    void orderPaid(Order order, UUID paymentId);

    void orderPaymentFailed(Order order, String reason);

    void orderCancelled(Order order, String reason, boolean wasPaid);

    void orderCompleted(Order order);

    void orderStatusChanged(Order order, OrderStatus previousStatus, OrderStatus newStatus, String reason);
}
