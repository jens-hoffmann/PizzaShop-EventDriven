package com.example.pizzashop.messaging;

import com.example.pizzashop.contracts.EventEnvelope;
import org.slf4j.MDC;

import java.util.UUID;

public final class EventMdc {

    public static final String CORRELATION_ID = "correlationId";
    public static final String CAUSATION_ID = "causationId";

    private EventMdc() {
    }

    public static void set(EventEnvelope<?> envelope) {
        MDC.put(CORRELATION_ID, envelope.correlationId().toString());
        MDC.put(CAUSATION_ID, envelope.eventId().toString());
    }

    public static void clear() {
        MDC.remove(CORRELATION_ID);
        MDC.remove(CAUSATION_ID);
    }

    public static UUID correlationId() {
        return uuid(MDC.get(CORRELATION_ID));
    }

    public static UUID causationId() {
        return uuid(MDC.get(CAUSATION_ID));
    }

    private static UUID uuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
