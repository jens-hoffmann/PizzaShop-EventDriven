package com.example.pizzashop.kitchen.application.service;

import com.example.pizzashop.kitchen.application.port.in.KitchenUseCases;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class KitchenTicketScheduler {

    private final KitchenUseCases useCases;

    KitchenTicketScheduler(KitchenUseCases useCases) {
        this.useCases = useCases;
    }

    @Scheduled(fixedDelayString = "${app.kitchen.poll-interval-ms:500}")
    public void processDueTickets() {
        useCases.processDueTickets();
    }
}
