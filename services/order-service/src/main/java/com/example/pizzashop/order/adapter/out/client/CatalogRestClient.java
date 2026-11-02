package com.example.pizzashop.order.adapter.out.client;

import com.example.pizzashop.order.application.port.out.CatalogClient;
import com.example.pizzashop.web.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CatalogRestClient implements CatalogClient {

    private static final Logger log = LoggerFactory.getLogger(CatalogRestClient.class);

    private final RestClient restClient;

    public CatalogRestClient(RestClient catalogRestClient) {
        this.restClient = catalogRestClient;
    }

    @Override
    public Map<UUID, CatalogPizza> findPizzas(Set<UUID> pizzaIds) {
        if (pizzaIds.isEmpty()) {
            return Map.of();
        }
        try {
            List<CatalogPizzaDto> response = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/api/v1/pizzas");
                        pizzaIds.forEach(id -> uriBuilder.queryParam("ids", id));
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<CatalogPizzaDto>>() {
                    });
            if (response == null) {
                return Map.of();
            }
            return response.stream()
                    .map(dto -> new CatalogPizza(
                            dto.id(),
                            dto.name(),
                            dto.available(),
                            dto.prices() == null ? Map.of() : dto.prices()
                    ))
                    .collect(Collectors.toMap(CatalogPizza::id, Function.identity()));
        } catch (RestClientException exception) {
            log.warn("Catalog lookup failed for pizzaIds={}", pizzaIds, exception);
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "CATALOG_UNAVAILABLE",
                    "The catalog is currently unavailable");
        }
    }

    private record CatalogPizzaDto(
            UUID id,
            String name,
            boolean available,
            Map<String, BigDecimal> prices
    ) {
    }
}
