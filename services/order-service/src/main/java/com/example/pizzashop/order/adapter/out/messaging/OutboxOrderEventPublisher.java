package com.example.pizzashop.order.adapter.out.messaging;

import com.example.pizzashop.contracts.DeliveryAddressSnapshot;
import com.example.pizzashop.contracts.EventEnvelope;
import com.example.pizzashop.contracts.EventTypes;
import com.example.pizzashop.contracts.OrderCancelledEvent;
import com.example.pizzashop.contracts.OrderCompletedEvent;
import com.example.pizzashop.contracts.OrderLineSnapshot;
import com.example.pizzashop.contracts.OrderPaidEvent;
import com.example.pizzashop.contracts.OrderPaymentFailedEvent;
import com.example.pizzashop.contracts.OrderPlacedEvent;
import com.example.pizzashop.contracts.OrderStatusChangedEvent;
import com.example.pizzashop.messaging.EventMdc;
import com.example.pizzashop.messaging.OutboxWriter;
import com.example.pizzashop.order.application.port.out.OrderEventPublisher;
import com.example.pizzashop.order.domain.model.Order;
import com.example.pizzashop.order.domain.model.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class OutboxOrderEventPublisher implements OrderEventPublisher {

    private static final String PRODUCER = "order-service";

    private final OutboxWriter outboxWriter;

    public OutboxOrderEventPublisher(OutboxWriter outboxWriter) {
        this.outboxWriter = outboxWriter;
    }

    @Override
    public void orderPlaced(Order order) {
        publish(EventTypes.ORDER_PLACED, order, new OrderPlacedEvent(
                order.id(),
                order.customerId(),
                items(order),
                order.total(),
                order.currency(),
                address(order),
                order.createdAt()
        ));
    }

    @Override
    public void orderPaid(Order order, UUID paymentId) {
        publish(EventTypes.ORDER_PAID, order, new OrderPaidEvent(
                order.id(),
                paymentId,
                order.updatedAt()
        ));
    }

    @Override
    public void orderPaymentFailed(Order order, String reason) {
        publish(EventTypes.ORDER_PAYMENT_FAILED, order, new OrderPaymentFailedEvent(
                order.id(),
                reason,
                order.updatedAt()
        ));
    }

    @Override
    public void orderCancelled(Order order, String reason, boolean wasPaid) {
        publish(EventTypes.ORDER_CANCELLED, order, new OrderCancelledEvent(
                order.id(),
                reason,
                order.updatedAt(),
                wasPaid
        ));
    }

    @Override
    public void orderCompleted(Order order) {
        publish(EventTypes.ORDER_COMPLETED, order, new OrderCompletedEvent(
                order.id(),
                order.updatedAt()
        ));
    }

    @Override
    public void orderStatusChanged(
            Order order,
            OrderStatus previousStatus,
            OrderStatus newStatus,
            String reason
    ) {
        publish(EventTypes.ORDER_STATUS_CHANGED, order, new OrderStatusChangedEvent(
                order.id(),
                order.customerId(),
                previousStatus == null ? null : previousStatus.name(),
                newStatus.name(),
                reason,
                order.updatedAt()
        ));
    }

    private void publish(String eventType, Order order, Object payload) {
        UUID correlationId = EventMdc.correlationId();
        if (correlationId == null) {
            correlationId = UUID.randomUUID();
        }
        outboxWriter.append(EventEnvelope.create(
                eventType,
                PRODUCER,
                correlationId,
                EventMdc.causationId(),
                order.id().toString(),
                payload
        ));
    }

    private List<OrderLineSnapshot> items(Order order) {
        return order.items().stream()
                .map(item -> new OrderLineSnapshot(
                        item.pizzaId(),
                        item.name(),
                        item.size(),
                        item.unitPrice(),
                        item.quantity(),
                        item.lineTotal()
                ))
                .toList();
    }

    private DeliveryAddressSnapshot address(Order order) {
        return new DeliveryAddressSnapshot(
                order.deliveryAddress().street(),
                order.deliveryAddress().city(),
                order.deliveryAddress().postalCode()
        );
    }
}
