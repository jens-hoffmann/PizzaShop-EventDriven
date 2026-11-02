package com.example.pizzashop.payment.adapter.in.rest;

import com.example.pizzashop.payment.adapter.in.rest.generated.api.PaymentsApi;
import com.example.pizzashop.payment.adapter.in.rest.generated.model.PaymentResponse;
import com.example.pizzashop.payment.application.port.in.PaymentUseCases;
import com.example.pizzashop.payment.domain.model.Payment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneOffset;
import java.util.UUID;

@RestController
class PaymentRestController implements PaymentsApi {

    private final PaymentUseCases useCases;

    PaymentRestController(PaymentUseCases useCases) {
        this.useCases = useCases;
    }

    @Override
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(UUID orderId) {
        return ResponseEntity.ok(toResponse(useCases.getByOrderId(orderId)));
    }

    private PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse(
                payment.id(),
                payment.orderId(),
                payment.amount().doubleValue(),
                payment.currency(),
                com.example.pizzashop.payment.adapter.in.rest.generated.model.PaymentStatus.valueOf(
                        payment.status().name()),
                payment.createdAt().atOffset(ZoneOffset.UTC),
                payment.updatedAt().atOffset(ZoneOffset.UTC)
        );
        response.setFailureReason(payment.failureReason());
        return response;
    }
}
