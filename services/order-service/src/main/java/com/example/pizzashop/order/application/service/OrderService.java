package com.example.pizzashop.order.application.service;

import com.example.pizzashop.contracts.EventEnvelope;
import com.example.pizzashop.contracts.KitchenTicketAcceptedEvent;
import com.example.pizzashop.contracts.KitchenTicketCancelledEvent;
import com.example.pizzashop.contracts.OrderReadyEvent;
import com.example.pizzashop.contracts.PaymentFailedEvent;
import com.example.pizzashop.contracts.PaymentRefundedEvent;
import com.example.pizzashop.contracts.PaymentSucceededEvent;
import com.example.pizzashop.contracts.PreparationStartedEvent;
import com.example.pizzashop.order.application.port.in.OrderUseCases;
import com.example.pizzashop.order.application.port.out.CatalogClient;
import com.example.pizzashop.order.application.port.out.IdempotencyStore;
import com.example.pizzashop.order.application.port.out.IdempotencyStore.IdempotencyRecord;
import com.example.pizzashop.order.application.port.out.OrderEventPublisher;
import com.example.pizzashop.order.application.port.out.OrderHistoryRepository;
import com.example.pizzashop.order.application.port.out.OrderRepository;
import com.example.pizzashop.order.domain.model.DeliveryAddress;
import com.example.pizzashop.order.domain.model.Order;
import com.example.pizzashop.order.domain.model.OrderItem;
import com.example.pizzashop.order.domain.model.OrderStatus;
import com.example.pizzashop.order.domain.model.OrderStatusHistoryEntry;
import com.example.pizzashop.web.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderService implements OrderUseCases {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);
    private static final String DEFAULT_CANCEL_REASON = "Customer cancellation";

    private final OrderRepository repository;
    private final OrderHistoryRepository historyRepository;
    private final IdempotencyStore idempotencyStore;
    private final CatalogClient catalogClient;
    private final OrderEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public OrderService(
            OrderRepository repository,
            OrderHistoryRepository historyRepository,
            IdempotencyStore idempotencyStore,
            CatalogClient catalogClient,
            OrderEventPublisher eventPublisher,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.idempotencyStore = idempotencyStore;
        this.catalogClient = catalogClient;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    @Transactional
    public PlaceOrderResult place(CreateOrderCommand command, String idempotencyKey) {
        validateIdempotencyKey(idempotencyKey);
        Instant now = Instant.now(clock);
        String requestHash = requestHash(command);

        Optional<IdempotencyRecord> existing = idempotencyStore.findByKey(idempotencyKey);
        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            if (record.expiresAt().isBefore(now)) {
                idempotencyStore.delete(idempotencyKey);
            } else if (!record.requestHash().equals(requestHash)) {
                throw new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUSED",
                        "The Idempotency-Key was already used with a different request");
            } else {
                Order order = repository.findById(record.orderId())
                        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "IDEMPOTENCY_RECORD_BROKEN", "The original order could not be found"));
                return new PlaceOrderResult(order, true);
            }
        }

        List<OrderItem> items = priceItems(command.items());
        DeliveryAddress address = new DeliveryAddress(
                command.deliveryAddress().street(),
                command.deliveryAddress().city(),
                command.deliveryAddress().postalCode()
        );
        Order order = Order.place(UUID.randomUUID(), command.customerId(), items, address, now);
        Order saved = repository.save(order);

        historyRepository.append(new OrderStatusHistoryEntry(
                saved.id(), OrderStatus.PLACED, "Order placed", now, null));
        idempotencyStore.save(new IdempotencyRecord(
                idempotencyKey, requestHash, saved.id(), now, now.plus(IDEMPOTENCY_TTL)));

        eventPublisher.orderPlaced(saved);
        eventPublisher.orderStatusChanged(saved, null, OrderStatus.PLACED, "Order placed");
        return new PlaceOrderResult(saved, false);
    }

    @Override
    @Transactional(readOnly = true)
    public Order get(UUID id) {
        return repository.findById(id).orElseThrow(() -> notFound(id));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderPageResult list(String customerId, OrderStatus status, int page, int size) {
        if (page < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGE", "page must be zero or greater");
        }
        if (size < 1 || size > 100) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_SIZE", "size must be between 1 and 100");
        }
        return repository.findPage(customerId, status, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusHistoryEntry> history(UUID id) {
        get(id);
        return historyRepository.findByOrderId(id);
    }

    @Override
    @Transactional
    public Order cancel(UUID id, String reason) {
        Order order = get(id);
        if (order.status() == OrderStatus.CANCELLED) {
            return order;
        }
        if (!order.canBeCancelled()) {
            throw new ApiException(HttpStatus.CONFLICT, "ORDER_NOT_CANCELLABLE",
                    "Order " + id + " cannot be cancelled in status " + order.status());
        }
        OrderStatus previous = order.status();
        boolean wasPaid = order.wasPaid();
        String cancelReason = reason == null || reason.isBlank() ? DEFAULT_CANCEL_REASON : reason.trim();
        Instant now = Instant.now(clock);

        order.cancel(now);
        Order saved = repository.save(order);
        historyRepository.append(new OrderStatusHistoryEntry(
                saved.id(), OrderStatus.CANCELLED, cancelReason, now, null));
        eventPublisher.orderCancelled(saved, cancelReason, wasPaid);
        eventPublisher.orderStatusChanged(saved, previous, OrderStatus.CANCELLED, cancelReason);
        return saved;
    }

    @Override
    @Transactional
    public Order complete(UUID id) {
        Order order = get(id);
        if (order.status() == OrderStatus.COMPLETED) {
            return order;
        }
        if (order.status() != OrderStatus.READY) {
            throw new ApiException(HttpStatus.CONFLICT, "ORDER_NOT_READY",
                    "Order " + id + " must be READY before it can be completed");
        }
        Instant now = Instant.now(clock);
        order.complete(now);
        Order saved = repository.save(order);
        historyRepository.append(new OrderStatusHistoryEntry(
                saved.id(), OrderStatus.COMPLETED, "Completed by staff", now, null));
        eventPublisher.orderCompleted(saved);
        eventPublisher.orderStatusChanged(saved, OrderStatus.READY, OrderStatus.COMPLETED, "Completed by staff");
        return saved;
    }

    @Override
    @Transactional
    public void onPaymentSucceeded(EventEnvelope<PaymentSucceededEvent> event) {
        Order order = get(event.payload().orderId());
        if (order.status() == OrderStatus.PAID) {
            log.debug("Ignoring duplicate PaymentSucceeded eventId={} orderId={}", event.eventId(), order.id());
            return;
        }
        if (order.status() != OrderStatus.PLACED) {
            log.warn("Ignoring PaymentSucceeded for orderId={} in status {}", order.id(), order.status());
            return;
        }
        Instant now = Instant.now(clock);
        order.markPaid(now);
        Order saved = repository.save(order);
        historyRepository.append(new OrderStatusHistoryEntry(
                saved.id(), OrderStatus.PAID, "Payment succeeded", now, event.eventId()));
        eventPublisher.orderPaid(saved, event.payload().paymentId());
        eventPublisher.orderStatusChanged(saved, OrderStatus.PLACED, OrderStatus.PAID, "Payment succeeded");
    }

    @Override
    @Transactional
    public void onPaymentFailed(EventEnvelope<PaymentFailedEvent> event) {
        Order order = get(event.payload().orderId());
        if (order.status() == OrderStatus.PAYMENT_FAILED) {
            log.debug("Ignoring duplicate PaymentFailed eventId={} orderId={}", event.eventId(), order.id());
            return;
        }
        if (order.status() != OrderStatus.PLACED) {
            log.warn("Ignoring PaymentFailed for orderId={} in status {}", order.id(), order.status());
            return;
        }
        Instant now = Instant.now(clock);
        order.markPaymentFailed(now);
        Order saved = repository.save(order);
        historyRepository.append(new OrderStatusHistoryEntry(
                saved.id(), OrderStatus.PAYMENT_FAILED, event.payload().reason(), now, event.eventId()));
        eventPublisher.orderPaymentFailed(saved, event.payload().reason());
        eventPublisher.orderStatusChanged(
                saved, OrderStatus.PLACED, OrderStatus.PAYMENT_FAILED, event.payload().reason());
    }

    @Override
    public void onPaymentRefunded(EventEnvelope<PaymentRefundedEvent> event) {
        log.info("Payment refunded paymentId={} orderId={} amount={}",
                event.payload().paymentId(), event.payload().orderId(), event.payload().amount());
    }

    @Override
    @Transactional
    public void onKitchenTicketAccepted(EventEnvelope<KitchenTicketAcceptedEvent> event) {
        Order order = get(event.payload().orderId());
        if (order.status() == OrderStatus.CONFIRMED) {
            log.debug("Ignoring duplicate KitchenTicketAccepted eventId={} orderId={}", event.eventId(), order.id());
            return;
        }
        if (order.status() != OrderStatus.PAID) {
            log.warn("Ignoring KitchenTicketAccepted for orderId={} in status {}", order.id(), order.status());
            return;
        }
        Instant now = Instant.now(clock);
        order.confirm(now);
        Order saved = repository.save(order);
        historyRepository.append(new OrderStatusHistoryEntry(
                saved.id(), OrderStatus.CONFIRMED, "Kitchen accepted the order", now, event.eventId()));
        eventPublisher.orderStatusChanged(
                saved, OrderStatus.PAID, OrderStatus.CONFIRMED, "Kitchen accepted the order");
    }

    @Override
    @Transactional
    public void onPreparationStarted(EventEnvelope<PreparationStartedEvent> event) {
        Order order = get(event.payload().orderId());
        if (order.status() == OrderStatus.PREPARING) {
            log.debug("Ignoring duplicate PreparationStarted eventId={} orderId={}", event.eventId(), order.id());
            return;
        }
        if (order.status() != OrderStatus.CONFIRMED) {
            log.warn("Ignoring PreparationStarted for orderId={} in status {}", order.id(), order.status());
            return;
        }
        Instant now = Instant.now(clock);
        order.startPreparation(now);
        Order saved = repository.save(order);
        historyRepository.append(new OrderStatusHistoryEntry(
                saved.id(), OrderStatus.PREPARING, "Preparation started", now, event.eventId()));
        eventPublisher.orderStatusChanged(
                saved, OrderStatus.CONFIRMED, OrderStatus.PREPARING, "Preparation started");
    }

    @Override
    @Transactional
    public void onOrderReady(EventEnvelope<OrderReadyEvent> event) {
        Order order = get(event.payload().orderId());
        if (order.status() == OrderStatus.READY) {
            log.debug("Ignoring duplicate OrderReady eventId={} orderId={}", event.eventId(), order.id());
            return;
        }
        if (order.status() != OrderStatus.PREPARING) {
            log.warn("Ignoring OrderReady for orderId={} in status {}", order.id(), order.status());
            return;
        }
        Instant now = Instant.now(clock);
        order.markReady(now);
        Order saved = repository.save(order);
        historyRepository.append(new OrderStatusHistoryEntry(
                saved.id(), OrderStatus.READY, "Order is ready", now, event.eventId()));
        eventPublisher.orderStatusChanged(
                saved, OrderStatus.PREPARING, OrderStatus.READY, "Order is ready");
    }

    @Override
    public void onKitchenTicketCancelled(EventEnvelope<KitchenTicketCancelledEvent> event) {
        log.info("Kitchen ticket cancelled ticketId={} orderId={} reason={}",
                event.payload().ticketId(), event.payload().orderId(), event.payload().reason());
    }

    private List<OrderItem> priceItems(List<OrderItemCommand> items) {
        if (items == null || items.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_ORDER", "An order needs at least one item");
        }
        Set<UUID> pizzaIds = items.stream()
                .map(OrderItemCommand::pizzaId)
                .collect(java.util.stream.Collectors.toSet());
        Map<UUID, CatalogClient.CatalogPizza> pizzas = catalogClient.findPizzas(pizzaIds);

        try {
            return items.stream()
                    .map(item -> toOrderItem(item, pizzas))
                    .toList();
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_ORDER_ITEM", exception.getMessage());
        }
    }

    private OrderItem toOrderItem(
            OrderItemCommand item,
            Map<UUID, CatalogClient.CatalogPizza> pizzas
    ) {
        CatalogClient.CatalogPizza pizza = pizzas.get(item.pizzaId());
        if (pizza == null) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "PIZZA_NOT_FOUND",
                    "Pizza " + item.pizzaId() + " does not exist");
        }
        if (!pizza.available()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "PIZZA_UNAVAILABLE",
                    "Pizza " + pizza.name() + " is currently unavailable");
        }
        BigDecimal price = pizza.prices().get(item.size());
        if (price == null) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "PIZZA_SIZE_UNAVAILABLE",
                    "Pizza " + pizza.name() + " has no price for size " + item.size());
        }
        return OrderItem.create(item.pizzaId(), pizza.name(), item.size(), price, item.quantity());
    }

    private String requestHash(CreateOrderCommand command) {
        try {
            byte[] json = objectMapper.writeValueAsString(command).getBytes(StandardCharsets.UTF_8);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(json);
            return HexFormat.of().formatHex(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash order request", exception);
        }
    }

    private static void validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.length() < 8 || idempotencyKey.length() > 128) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_IDEMPOTENCY_KEY",
                    "Idempotency-Key must be between 8 and 128 characters");
        }
    }

    private static ApiException notFound(UUID id) {
        return new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Order " + id + " not found");
    }
}
