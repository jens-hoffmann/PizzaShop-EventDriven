package com.example.pizzashop.catalog.adapter.out.messaging;

import com.example.pizzashop.catalog.application.port.out.CatalogEventPublisher;
import com.example.pizzashop.catalog.domain.model.Pizza;
import com.example.pizzashop.contracts.EventEnvelope;
import com.example.pizzashop.contracts.EventTypes;
import com.example.pizzashop.contracts.PizzaAvailabilityChangedEvent;
import com.example.pizzashop.contracts.PizzaSnapshot;
import com.example.pizzashop.messaging.OutboxWriter;
import com.example.pizzashop.web.CorrelationIdFilter;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OutboxCatalogEventPublisher implements CatalogEventPublisher {

    private static final String PRODUCER = "catalog-service";

    private final OutboxWriter outboxWriter;

    public OutboxCatalogEventPublisher(OutboxWriter outboxWriter) {
        this.outboxWriter = outboxWriter;
    }

    @Override
    public void pizzaCreated(Pizza pizza) {
        publish(EventTypes.PIZZA_CREATED, pizza, snapshot(pizza));
    }

    @Override
    public void pizzaUpdated(Pizza pizza) {
        publish(EventTypes.PIZZA_UPDATED, pizza, snapshot(pizza));
    }

    @Override
    public void pizzaAvailabilityChanged(Pizza pizza) {
        publish(EventTypes.PIZZA_AVAILABILITY_CHANGED, pizza,
                new PizzaAvailabilityChangedEvent(pizza.id(), pizza.available(), pizza.updatedAt()));
    }

    private void publish(String eventType, Pizza pizza, Object payload) {
        outboxWriter.append(EventEnvelope.create(
                eventType,
                PRODUCER,
                correlationId(),
                null,
                pizza.id().toString(),
                payload
        ));
    }

    private PizzaSnapshot snapshot(Pizza pizza) {
        Map<String, BigDecimal> prices = pizza.prices().entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().name(), Map.Entry::getValue));
        return new PizzaSnapshot(
                pizza.id(),
                pizza.name(),
                pizza.description(),
                pizza.available(),
                prices,
                pizza.updatedAt()
        );
    }

    private UUID correlationId() {
        String value = MDC.get(CorrelationIdFilter.MDC_KEY);
        if (value == null || value.isBlank()) {
            return UUID.randomUUID();
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return UUID.randomUUID();
        }
    }
}
