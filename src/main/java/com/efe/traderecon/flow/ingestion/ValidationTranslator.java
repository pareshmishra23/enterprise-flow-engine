package com.efe.traderecon.flow.ingestion;

import com.efe.traderecon.api.dto.ReconciliationJobRequest;
import com.efe.traderecon.api.dto.TradeRecordDto;
import com.efe.traderecon.ikasan.model.IkasanTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ValidationTranslator implements IkasanTranslator<ReconciliationJobRequest> {
    private static final Logger log = LoggerFactory.getLogger(ValidationTranslator.class);

    @Override
    public String getName() {
        return "validation-translator";
    }

    @Override
    public ReconciliationJobRequest translate(ReconciliationJobRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Validation failed: Request is null");
        }
        if (request.getBusinessDate() == null) {
            throw new IllegalArgumentException("Validation failed: 'businessDate' is required");
        }
        if (request.getSource() == null || request.getSource().isBlank()) {
            throw new IllegalArgumentException("Validation failed: 'source' is required");
        }
        if (request.getRecords() == null) {
            throw new IllegalArgumentException("Validation failed: 'records' list cannot be null");
        }

        // Validate trade items if present
        for (TradeRecordDto record : request.getRecords()) {
            if (record.getTradeId() == null || record.getTradeId().isBlank()) {
                throw new IllegalArgumentException("Validation failed: tradeId cannot be empty");
            }
        }

        // Normalize source
        request.setSource(request.getSource().trim().toUpperCase());
        log.info("Validation passed for source [{}] date [{}] recordCount [{}]",
                request.getSource(), request.getBusinessDate(), request.getRecords().size());
        return request;
    }
}
