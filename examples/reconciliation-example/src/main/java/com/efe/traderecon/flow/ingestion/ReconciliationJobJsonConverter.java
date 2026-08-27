package com.efe.traderecon.flow.ingestion;

import com.efe.traderecon.api.dto.ReconciliationJobRequest;
import com.efe.traderecon.ikasan.model.IkasanConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ReconciliationJobJsonConverter implements IkasanConverter<Object, ReconciliationJobRequest> {
    private static final Logger log = LoggerFactory.getLogger(ReconciliationJobJsonConverter.class);

    private final ObjectMapper objectMapper;

    public ReconciliationJobJsonConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "reconciliation-job-json-converter";
    }

    @Override
    public ReconciliationJobRequest convert(Object source) {
        if (source == null) {
            throw new IllegalArgumentException("Payload cannot be null");
        }
        if (source instanceof ReconciliationJobRequest req) {
            return req;
        }
        try {
            if (source instanceof String jsonStr) {
                return objectMapper.readValue(jsonStr, ReconciliationJobRequest.class);
            }
            return objectMapper.convertValue(source, ReconciliationJobRequest.class);
        } catch (Exception e) {
            log.error("Failed to convert payload to ReconciliationJobRequest: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid reconciliation payload format: " + e.getMessage(), e);
        }
    }
}
