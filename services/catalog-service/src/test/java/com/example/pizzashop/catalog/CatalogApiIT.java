package com.example.pizzashop.catalog;

import com.example.pizzashop.catalog.adapter.in.rest.generated.model.PizzaResponse;
import com.example.pizzashop.catalog.adapter.in.rest.generated.model.PizzaUpsertRequest;
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
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SharedTestContainersConfiguration.class)
@TestPropertySource(properties = {
        "app.outbox.poll-interval-ms=100",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
class CatalogApiIT {

    @LocalServerPort
    private int port;

    private RestClient client;

    @BeforeEach
    void setUp() {
        client = RestClient.create("http://localhost:" + port);
    }

    @Test
    void createsAndListsAPizza() {
        PizzaUpsertRequest request = new PizzaUpsertRequest(
                "IT Pizza " + UUID.randomUUID(),
                Map.of("SMALL", 5.0, "MEDIUM", 6.0, "LARGE", 7.0)
        );
        request.setDescription("Integration test pizza");

        ResponseEntity<PizzaResponse> created = client.post()
                .uri("/api/v1/admin/pizzas")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(PizzaResponse.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().getId()).isNotNull();
        assertThat(created.getBody().getPrices()).containsEntry("LARGE", 7.0);

        List<PizzaResponse> pizzas = client.get()
                .uri("/api/v1/pizzas")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        assertThat(pizzas)
                .extracting(PizzaResponse::getId)
                .contains(created.getBody().getId());
    }

    @Test
    void rejectsAnInvalidPizzaRequest() {
        PizzaUpsertRequest request = new PizzaUpsertRequest("Invalid", Map.of());

        assertThatThrownBy(() -> client.post()
                .uri("/api/v1/admin/pizzas")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity())
                .isInstanceOfSatisfying(RestClientResponseException.class,
                        exception -> assertThat(exception.getStatusCode().value()).isEqualTo(400));
    }
}
