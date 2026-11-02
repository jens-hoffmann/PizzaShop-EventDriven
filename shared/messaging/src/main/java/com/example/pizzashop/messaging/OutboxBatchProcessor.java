package com.example.pizzashop.messaging;

import com.example.pizzashop.contracts.Topics;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OutboxBatchProcessor {

    private static final Logger log = LoggerFactory.getLogger(OutboxBatchProcessor.class);

    private final OutboxRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxProperties properties;

    public OutboxBatchProcessor(
            OutboxRepository repository,
            KafkaTemplate<String, String> kafkaTemplate,
            OutboxProperties properties
    ) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    @Transactional
    public void publishBatch() {
        Instant now = Instant.now();
        List<OutboxMessage> messages = repository.findDue(now, properties.getBatchSize());
        for (OutboxMessage message : messages) {
            publish(message, now);
        }
    }

    private void publish(OutboxMessage message, Instant now) {
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    Topics.topicFor(message.getEventType()),
                    message.getAggregateId(),
                    message.getPayload()
            );
            record.headers().add(header("eventId", message.getId().toString()));
            record.headers().add(header("eventType", message.getEventType()));
            record.headers().add(header("eventVersion", Integer.toString(message.getEventVersion())));

            kafkaTemplate.send(record).get(properties.getSendTimeoutMs(), TimeUnit.MILLISECONDS);
            message.markPublished(Instant.now());
            log.info("Published outbox event eventId={} eventType={} aggregateId={}",
                    message.getId(), message.getEventType(), message.getAggregateId());
        } catch (Exception exception) {
            message.markFailed(rootMessage(exception), now, properties);
            log.error("Failed to publish outbox event eventId={} eventType={} attempts={}",
                    message.getId(), message.getEventType(), message.getAttempts(), exception);
        } finally {
            repository.save(message);
        }
    }

    private static RecordHeader header(String name, String value) {
        return new RecordHeader(name, value.getBytes(StandardCharsets.UTF_8));
    }

    private static String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current.getClass().getSimpleName() + ": " + current.getMessage();
    }
}
