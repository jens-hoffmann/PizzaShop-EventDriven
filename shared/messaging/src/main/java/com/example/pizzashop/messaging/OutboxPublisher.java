package com.example.pizzashop.messaging;

import org.springframework.scheduling.annotation.Scheduled;

public class OutboxPublisher {

    private final OutboxBatchProcessor processor;

    public OutboxPublisher(OutboxBatchProcessor processor) {
        this.processor = processor;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:500}")
    public void publishPending() {
        processor.publishBatch();
    }
}
