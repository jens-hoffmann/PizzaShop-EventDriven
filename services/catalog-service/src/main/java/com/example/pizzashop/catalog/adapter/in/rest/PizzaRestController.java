package com.example.pizzashop.catalog.adapter.in.rest;

import com.example.pizzashop.catalog.adapter.in.rest.generated.api.AdminPizzasApi;
import com.example.pizzashop.catalog.adapter.in.rest.generated.api.PizzasApi;
import com.example.pizzashop.catalog.adapter.in.rest.generated.model.AvailabilityRequest;
import com.example.pizzashop.catalog.adapter.in.rest.generated.model.PizzaResponse;
import com.example.pizzashop.catalog.adapter.in.rest.generated.model.PizzaUpsertRequest;
import com.example.pizzashop.catalog.application.port.in.CatalogUseCases;
import com.example.pizzashop.catalog.application.port.in.CatalogUseCases.CreatePizzaCommand;
import com.example.pizzashop.catalog.application.port.in.CatalogUseCases.UpdatePizzaCommand;
import com.example.pizzashop.catalog.domain.model.Pizza;
import com.example.pizzashop.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
class PizzaRestController implements PizzasApi, AdminPizzasApi {

    private final CatalogUseCases useCases;

    PizzaRestController(CatalogUseCases useCases) {
        this.useCases = useCases;
    }

    @Override
    public ResponseEntity<List<PizzaResponse>> listPizzas(List<UUID> ids, Boolean available) {
        List<PizzaResponse> pizzas = useCases
                .list(Optional.ofNullable(ids), Optional.ofNullable(available))
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(pizzas);
    }

    @Override
    public ResponseEntity<PizzaResponse> getPizza(UUID id) {
        return ResponseEntity.ok(toResponse(useCases.get(id)));
    }

    @Override
    public ResponseEntity<PizzaResponse> createPizza(PizzaUpsertRequest request) {
        Pizza pizza = useCases.create(toCreateCommand(request));
        return ResponseEntity
                .created(URI.create("/api/v1/pizzas/" + pizza.id()))
                .body(toResponse(pizza));
    }

    @Override
    public ResponseEntity<PizzaResponse> updatePizza(UUID id, PizzaUpsertRequest request) {
        return ResponseEntity.ok(toResponse(useCases.update(id, toUpdateCommand(request))));
    }

    @Override
    public ResponseEntity<Void> deletePizza(UUID id) {
        useCases.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<PizzaResponse> updatePizzaAvailability(UUID id, AvailabilityRequest request) {
        return ResponseEntity.ok(toResponse(useCases.changeAvailability(id, request.getAvailable())));
    }

    private CreatePizzaCommand toCreateCommand(PizzaUpsertRequest request) {
        return new CreatePizzaCommand(
                request.getName(),
                request.getDescription(),
                request.getAvailable() == null || request.getAvailable(),
                toPrices(request.getPrices())
        );
    }

    private UpdatePizzaCommand toUpdateCommand(PizzaUpsertRequest request) {
        return new UpdatePizzaCommand(
                request.getName(),
                request.getDescription(),
                request.getAvailable() == null || request.getAvailable(),
                toPrices(request.getPrices())
        );
    }

    private Map<String, BigDecimal> toPrices(Map<String, Double> prices) {
        if (prices == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PRICES", "Prices are required");
        }
        Map<String, BigDecimal> result = new HashMap<>();
        for (Map.Entry<String, Double> entry : prices.entrySet()) {
            if (entry.getValue() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PRICES",
                        "Price for size " + entry.getKey() + " is required");
            }
            result.put(entry.getKey(), BigDecimal.valueOf(entry.getValue()));
        }
        return result;
    }

    private PizzaResponse toResponse(Pizza pizza) {
        Map<String, Double> prices = pizza.prices().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().name(),
                        entry -> entry.getValue().doubleValue()
                ));
        PizzaResponse response = new PizzaResponse(
                pizza.id(),
                pizza.name(),
                pizza.available(),
                prices,
                pizza.createdAt().atOffset(ZoneOffset.UTC),
                pizza.updatedAt().atOffset(ZoneOffset.UTC)
        );
        response.setDescription(pizza.description());
        return response;
    }
}
