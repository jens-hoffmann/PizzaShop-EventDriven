package com.example.pizzashop.order.adapter.in.rest;

import com.example.pizzashop.order.adapter.in.rest.generated.api.AdminOrdersApi;
import com.example.pizzashop.order.adapter.in.rest.generated.api.OrdersApi;
import com.example.pizzashop.order.adapter.in.rest.generated.model.CancelOrderRequest;
import com.example.pizzashop.order.adapter.in.rest.generated.model.CreateOrderRequest;
import com.example.pizzashop.order.adapter.in.rest.generated.model.DeliveryAddress;
import com.example.pizzashop.order.adapter.in.rest.generated.model.OrderItemRequest;
import com.example.pizzashop.order.adapter.in.rest.generated.model.OrderItemResponse;
import com.example.pizzashop.order.adapter.in.rest.generated.model.OrderPage;
import com.example.pizzashop.order.adapter.in.rest.generated.model.OrderResponse;
import com.example.pizzashop.order.adapter.in.rest.generated.model.PizzaSize;
import com.example.pizzashop.order.application.port.in.OrderUseCases;
import com.example.pizzashop.order.application.port.in.OrderUseCases.CreateOrderCommand;
import com.example.pizzashop.order.application.port.in.OrderUseCases.DeliveryAddressCommand;
import com.example.pizzashop.order.application.port.in.OrderUseCases.OrderItemCommand;
import com.example.pizzashop.order.application.port.in.OrderUseCases.OrderPageResult;
import com.example.pizzashop.order.application.port.in.OrderUseCases.PlaceOrderResult;
import com.example.pizzashop.order.domain.model.Order;
import com.example.pizzashop.order.domain.model.OrderStatus;
import com.example.pizzashop.order.domain.model.OrderStatusHistoryEntry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@RestController
class OrderRestController implements OrdersApi, AdminOrdersApi {

    private final OrderUseCases useCases;

    OrderRestController(OrderUseCases useCases) {
        this.useCases = useCases;
    }

    @Override
    public ResponseEntity<OrderResponse> createOrder(String idempotencyKey, CreateOrderRequest request) {
        PlaceOrderResult result = useCases.place(toCommand(request), idempotencyKey);
        HttpStatus status = result.replayed() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(toResponse(result.order()));
    }

    @Override
    public ResponseEntity<OrderResponse> getOrder(UUID id) {
        return ResponseEntity.ok(toResponse(useCases.get(id)));
    }

    @Override
    public ResponseEntity<OrderPage> listOrders(
            String customerId,
            com.example.pizzashop.order.adapter.in.rest.generated.model.OrderStatus status,
            Integer page,
            Integer size
    ) {
        OrderPageResult result = useCases.list(
                customerId,
                status == null ? null : OrderStatus.valueOf(status.name()),
                page == null ? 0 : page,
                size == null ? 20 : size
        );
        OrderPage response = new OrderPage(
                result.content().stream().map(this::toResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<com.example.pizzashop.order.adapter.in.rest.generated.model.OrderStatusHistoryEntry>>
    getOrderHistory(UUID id) {
        List<com.example.pizzashop.order.adapter.in.rest.generated.model.OrderStatusHistoryEntry> history =
                useCases.history(id).stream().map(this::toResponse).toList();
        return ResponseEntity.ok(history);
    }

    @Override
    public ResponseEntity<OrderResponse> cancelOrder(UUID id, CancelOrderRequest request) {
        String reason = request == null ? null : request.getReason();
        return ResponseEntity.ok(toResponse(useCases.cancel(id, reason)));
    }

    @Override
    public ResponseEntity<OrderResponse> completeOrder(UUID id) {
        return ResponseEntity.ok(toResponse(useCases.complete(id)));
    }

    private CreateOrderCommand toCommand(CreateOrderRequest request) {
        List<OrderItemCommand> items = request.getItems().stream()
                .map(this::toCommand)
                .toList();
        DeliveryAddress address = request.getDeliveryAddress();
        return new CreateOrderCommand(
                request.getCustomerId(),
                items,
                new DeliveryAddressCommand(address.getStreet(), address.getCity(), address.getPostalCode())
        );
    }

    private OrderItemCommand toCommand(OrderItemRequest request) {
        return new OrderItemCommand(
                request.getPizzaId(),
                request.getSize().name(),
                request.getQuantity()
        );
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.items().stream()
                .map(item -> new OrderItemResponse(
                        item.pizzaId(),
                        item.name(),
                        PizzaSize.valueOf(item.size()),
                        item.unitPrice().doubleValue(),
                        item.quantity(),
                        item.lineTotal().doubleValue()
                ))
                .toList();
        return new OrderResponse(
                order.id(),
                order.customerId(),
                com.example.pizzashop.order.adapter.in.rest.generated.model.OrderStatus.valueOf(order.status().name()),
                items,
                order.total().doubleValue(),
                order.currency(),
                new DeliveryAddress(
                        order.deliveryAddress().street(),
                        order.deliveryAddress().city(),
                        order.deliveryAddress().postalCode()
                ),
                order.createdAt().atOffset(ZoneOffset.UTC),
                order.updatedAt().atOffset(ZoneOffset.UTC)
        );
    }

    private com.example.pizzashop.order.adapter.in.rest.generated.model.OrderStatusHistoryEntry
    toResponse(OrderStatusHistoryEntry entry) {
        com.example.pizzashop.order.adapter.in.rest.generated.model.OrderStatusHistoryEntry response =
                new com.example.pizzashop.order.adapter.in.rest.generated.model.OrderStatusHistoryEntry(
                        com.example.pizzashop.order.adapter.in.rest.generated.model.OrderStatus.valueOf(
                                entry.status().name()),
                        entry.occurredAt().atOffset(ZoneOffset.UTC)
                );
        response.setReason(entry.reason());
        return response;
    }
}
