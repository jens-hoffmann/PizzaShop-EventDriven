package com.example.pizzashop.order;

import com.example.pizzashop.order.adapter.in.rest.generated.model.CancelOrderRequest;
import com.example.pizzashop.order.adapter.in.rest.generated.model.CreateOrderRequest;
import com.example.pizzashop.order.adapter.in.rest.generated.model.DeliveryAddress;
import com.example.pizzashop.order.adapter.in.rest.generated.model.OrderItemRequest;
import com.example.pizzashop.order.adapter.in.rest.generated.model.OrderResponse;
import com.example.pizzashop.order.adapter.in.rest.generated.model.OrderStatus;
import com.example.pizzashop.order.adapter.in.rest.generated.model.OrderStatusHistoryEntry;
import com.example.pizzashop.order.adapter.in.rest.generated.model.PizzaSize;
import com.example.pizzashop.order.application.port.out.CatalogClient;
import com.example.pizzashop.order.application.port.out.CatalogClient.CatalogPizza;
import com.example.pizzashop.test.SharedTestContainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SharedTestContainersConfiguration.class)
@TestPropertySource(properties = {
        "app.outbox.poll-interval-ms=100",
        "app.payment.delay-ms=0"
})
class OrderApiIT {

    private static final UUID PIZZA_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @LocalServerPort
    private int port;

    @MockitoBean
    private CatalogClient catalogClient;

    private RestClient client;

    @BeforeEach
    void setUp() {
        client = RestClient.create("http://localhost:" + port);
        when(catalogClient.findPizzas(anySet())).thenReturn(Map.of(
                PIZZA_ID,
                new CatalogPizza(
                        PIZZA_ID,
                        "Margherita",
                        true,
                        Map.of(
                                "SMALL", new BigDecimal("7.50"),
                                "MEDIUM", new BigDecimal("9.50"),
                                "LARGE", new BigDecimal("11.50")
                        )
                )
        ));
    }

    @Test
    void placesAnOrderAndReplaysTheSameIdempotencyKey() {
        String key = "it-key-" + UUID.randomUUID();
        CreateOrderRequest request = request("customer-it", 2);

        ResponseEntity<OrderResponse> created = postOrder(key, request);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(created.getBody().getTotal()).isEqualTo(23.0);

        ResponseEntity<OrderResponse> replay = postOrder(key, request);
        assertThat(replay.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(replay.getBody()).isNotNull();
        assertThat(replay.getBody().getId()).isEqualTo(created.getBody().getId());

        assertThatThrownBy(() -> postOrder(key, request("other-customer", 2)))
                .isInstanceOfSatisfying(RestClientResponseException.class,
                        exception -> assertThat(exception.getStatusCode().value()).isEqualTo(409));
    }

    @Test
    void cancelsAnOrderAndRecordsHistory() {
        ResponseEntity<OrderResponse> created = postOrder(
                "it-cancel-" + UUID.randomUUID(),
                request("customer-cancel", 1)
        );
        assertThat(created.getBody()).isNotNull();
        UUID orderId = created.getBody().getId();

        ResponseEntity<OrderResponse> cancelled = client.post()
                .uri("/api/v1/orders/{id}/cancel", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CancelOrderRequest().reason("Integration test"))
                .retrieve()
                .toEntity(OrderResponse.class);

        assertThat(cancelled.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cancelled.getBody()).isNotNull();
        assertThat(cancelled.getBody().getStatus()).isEqualTo(OrderStatus.CANCELLED);

        List<OrderStatusHistoryEntry> history = client.get()
                .uri("/api/v1/orders/{id}/history", orderId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        assertThat(history).isNotNull();
        assertThat(history).extracting(OrderStatusHistoryEntry::getStatus)
                .containsExactly(OrderStatus.PLACED, OrderStatus.CANCELLED);
    }

    private ResponseEntity<OrderResponse> postOrder(String key, CreateOrderRequest request) {
        return client.post()
                .uri("/api/v1/orders")
                .header("Idempotency-Key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(OrderResponse.class);
    }

    private CreateOrderRequest request(String customerId, int quantity) {
        return new CreateOrderRequest(
                customerId,
                List.of(new OrderItemRequest(PIZZA_ID, PizzaSize.LARGE, quantity)),
                new DeliveryAddress("Main Street 1", "Pizza Town", "12345")
        );
    }
}
