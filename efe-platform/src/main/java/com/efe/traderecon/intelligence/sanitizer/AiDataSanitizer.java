package com.efe.traderecon.intelligence.sanitizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EFE AI Data Sanitizer.
 *
 * Explicit boundary: all event payloads pass through this before being sent
 * to any AI/LLM provider. Masks sensitive fields to prevent PII leakage.
 *
 * The list of masked fields is configurable in application.yml.
 * Do not assume all event payloads can be safely sent directly to a model.
 */
@Component
public class AiDataSanitizer {

    private static final Logger log = LoggerFactory.getLogger(AiDataSanitizer.class);
    private static final String MASKED = "***MASKED***";

    private final SanitizerProperties properties;

    public AiDataSanitizer(SanitizerProperties properties) {
        this.properties = properties;
    }

    /**
     * Returns a sanitized copy of the payload with sensitive fields masked.
     * The original payload is not modified.
     */
    public Map<String, Object> sanitize(Map<String, Object> payload) {
        if (payload == null) {
            return new HashMap<>();
        }

        Map<String, Object> sanitized = new HashMap<>(payload);
        List<String> maskedFields = properties.getMaskedFields();

        if (maskedFields != null) {
            for (String field : maskedFields) {
                if (sanitized.containsKey(field)) {
                    sanitized.put(field, MASKED);
                    log.debug("Masked sensitive field [{}] before AI submission", field);
                }
            }
        }

        return sanitized;
    }
}
