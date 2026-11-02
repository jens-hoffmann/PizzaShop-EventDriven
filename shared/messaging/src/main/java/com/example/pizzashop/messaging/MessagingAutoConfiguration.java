package com.example.pizzashop.messaging;

import com.example.pizzashop.contracts.EventSchemaValidator;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.backoff.FixedBackOff;
import tools.jackson.databind.ObjectMapper;

@AutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(OutboxProperties.class)
public class MessagingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EventSchemaValidator eventSchemaValidator() {
        return new EventSchemaValidator();
    }

    @Bean
    @ConditionalOnMissingBean
    public EventEnvelopeParser eventEnvelopeParser(
            ObjectMapper objectMapper,
            EventSchemaValidator eventSchemaValidator
    ) {
        return new EventEnvelopeParser(objectMapper, eventSchemaValidator);
    }

    @Bean
    @ConditionalOnMissingBean
    public IdempotentEventHandler idempotentEventHandler(
            ProcessedEventRepository repository,
            ObjectMapper objectMapper
    ) {
        return new IdempotentEventHandler(repository, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public OutboxWriter outboxWriter(OutboxRepository repository, ObjectMapper objectMapper) {
        return new OutboxWriter(repository, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public OutboxBatchProcessor outboxBatchProcessor(
            OutboxRepository repository,
            KafkaTemplate<String, String> kafkaTemplate,
            OutboxProperties properties
    ) {
        return new OutboxBatchProcessor(repository, kafkaTemplate, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public OutboxPublisher outboxPublisher(OutboxBatchProcessor processor) {
        return new OutboxPublisher(processor);
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(record.topic() + ".DLT", 0)
        );
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1_000L, 3));
        handler.addNotRetryableExceptions(InvalidEventException.class);
        return handler;
    }
}
