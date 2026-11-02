package com.example.pizzashop.order.adapter.out.persistence;

import com.example.pizzashop.order.application.port.in.OrderUseCases.OrderPageResult;
import com.example.pizzashop.order.application.port.out.OrderRepository;
import com.example.pizzashop.order.domain.model.DeliveryAddress;
import com.example.pizzashop.order.domain.model.Order;
import com.example.pizzashop.order.domain.model.OrderItem;
import com.example.pizzashop.order.domain.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class OrderRepositoryAdapter implements OrderRepository {

    private final SpringDataOrderRepository repository;

    OrderRepositoryAdapter(SpringDataOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Order save(Order order) {
        return toDomain(repository.save(toEntity(order)));
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public OrderPageResult findPage(String customerId, OrderStatus status, int page, int size) {
        Page<OrderJpaEntity> result = repository.findPage(
                customerId,
                status,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return new OrderPageResult(
                result.getContent().stream().map(this::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    private Order toDomain(OrderJpaEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
                .map(item -> new OrderItem(
                        item.getPizzaId(),
                        item.getName(),
                        item.getSize(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getLineTotal()
                ))
                .toList();
        DeliveryAddress address = new DeliveryAddress(
                entity.getStreet(),
                entity.getCity(),
                entity.getPostalCode()
        );
        return Order.reconstitute(
                entity.getId(),
                entity.getCustomerId(),
                entity.getStatus(),
                items,
                entity.getTotal(),
                entity.getCurrency(),
                address,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    private OrderJpaEntity toEntity(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setId(order.id());
        entity.setCustomerId(order.customerId());
        entity.setStatus(order.status());
        entity.setTotal(order.total());
        entity.setCurrency(order.currency());
        entity.setStreet(order.deliveryAddress().street());
        entity.setCity(order.deliveryAddress().city());
        entity.setPostalCode(order.deliveryAddress().postalCode());
        entity.setCreatedAt(order.createdAt());
        entity.setUpdatedAt(order.updatedAt());
        entity.setVersion(order.version());

        for (OrderItem item : order.items()) {
            OrderItemJpaEntity itemEntity = new OrderItemJpaEntity();
            itemEntity.setId(UUID.randomUUID());
            itemEntity.setPizzaId(item.pizzaId());
            itemEntity.setName(item.name());
            itemEntity.setSize(item.size());
            itemEntity.setUnitPrice(item.unitPrice());
            itemEntity.setQuantity(item.quantity());
            itemEntity.setLineTotal(item.lineTotal());
            entity.addItem(itemEntity);
        }
        return entity;
    }
}
