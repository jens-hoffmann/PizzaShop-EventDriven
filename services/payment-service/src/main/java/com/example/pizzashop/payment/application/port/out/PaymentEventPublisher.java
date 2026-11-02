package com.example.pizzashop.payment.application.port.out;

import com.example.pizzashop.payment.domain.model.Payment;

public interface PaymentEventPublisher {

    void paymentSucceeded(Payment payment);

    void paymentFailed(Payment payment);

    void paymentRefunded(Payment payment);
}
