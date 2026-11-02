package com.example.pizzashop.order.application.port.out;

import com.example.pizzashop.order.application.port.in.OrderUseCases.OrderPageResult;
import com.example.pizzashop.order.domain.model.Order;
import com.example.pizzashop.order.domain.model.OrderStatus;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(UUID id);

    OrderPageResult findPage(String customerId, OrderStatus status, int page, int size);
}
