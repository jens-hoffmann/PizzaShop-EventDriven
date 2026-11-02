package com.example.pizzashop.kitchen.adapter.in.rest;

import com.example.pizzashop.kitchen.adapter.in.rest.generated.api.KitchenTicketsApi;
import com.example.pizzashop.kitchen.adapter.in.rest.generated.model.KitchenTicketResponse;
import com.example.pizzashop.kitchen.application.port.in.KitchenUseCases;
import com.example.pizzashop.kitchen.domain.model.KitchenTicket;
import com.example.pizzashop.kitchen.domain.model.KitchenTicketStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
class KitchenTicketRestController implements KitchenTicketsApi {

    private final KitchenUseCases useCases;

    KitchenTicketRestController(KitchenUseCases useCases) {
        this.useCases = useCases;
    }

    @Override
    public ResponseEntity<List<KitchenTicketResponse>> listKitchenTickets(
            com.example.pizzashop.kitchen.adapter.in.rest.generated.model.KitchenTicketStatus status
    ) {
        Optional<KitchenTicketStatus> filter = status == null
                ? Optional.empty()
                : Optional.of(KitchenTicketStatus.valueOf(status.name()));
        return ResponseEntity.ok(useCases.list(filter).stream().map(this::toResponse).toList());
    }

    @Override
    public ResponseEntity<KitchenTicketResponse> getKitchenTicket(UUID id) {
        return ResponseEntity.ok(toResponse(useCases.get(id)));
    }

    private KitchenTicketResponse toResponse(KitchenTicket ticket) {
        KitchenTicketResponse response = new KitchenTicketResponse(
                ticket.id(),
                ticket.orderId(),
                com.example.pizzashop.kitchen.adapter.in.rest.generated.model.KitchenTicketStatus.valueOf(
                        ticket.status().name()),
                ticket.acceptedAt().atOffset(ZoneOffset.UTC)
        );
        if (ticket.preparationStartedAt() != null) {
            response.setPreparationStartedAt(ticket.preparationStartedAt().atOffset(ZoneOffset.UTC));
        }
        if (ticket.readyAt() != null) {
            response.setReadyAt(ticket.readyAt().atOffset(ZoneOffset.UTC));
        }
        if (ticket.cancelledAt() != null) {
            response.setCancelledAt(ticket.cancelledAt().atOffset(ZoneOffset.UTC));
        }
        return response;
    }
}
