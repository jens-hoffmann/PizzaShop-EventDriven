package com.example.pizzashop.contracts;

import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public final class EventSchemaValidator {

    private static final String SCHEMA_RESOURCE = "/schemas/event-envelope.schema.json";

    private final Schema schema;

    public EventSchemaValidator() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        try (InputStream input = EventSchemaValidator.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("Missing event schema resource " + SCHEMA_RESOURCE);
            }
            this.schema = registry.getSchema(input);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load event schema " + SCHEMA_RESOURCE, exception);
        }
    }

    public List<String> validate(JsonNode envelope) {
        return schema.validate(envelope).stream()
                .map(Error::getMessage)
                .toList();
    }
}
