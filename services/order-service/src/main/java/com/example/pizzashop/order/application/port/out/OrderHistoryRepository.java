package com.example.pizzashop.order.application.port.out;

import com.example.pizzashop.order.domain.model.OrderStatusHistoryEntry;

import java.util.List;
import java.util.UUID;

public interface OrderHistoryRepository {

    void append(OrderStatusHistoryEntry entry);

    List<OrderStatusHistoryEntry> findByOrderId(UUID orderId);
}
