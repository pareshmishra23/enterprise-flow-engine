package com.efe.traderecon.api.controller;

import com.efe.traderecon.api.dto.ReconciliationJobRequest;
import com.efe.traderecon.api.dto.ReconciliationJobResponse;
import com.efe.traderecon.ikasan.model.IkasanFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Domain-specific Reconciliation Ingestion Endpoint.
 * Delegates directly into the Ikasan trade-ingestion-flow.
 */
@RestController
@RequestMapping("/api/v1/jobs/reconciliation")
public class ReconciliationJobController {
    private static final Logger log = LoggerFactory.getLogger(ReconciliationJobController.class);

    private final IkasanFlow tradeIngestionFlow;

    public ReconciliationJobController(@Qualifier("tradeIngestionFlow") IkasanFlow tradeIngestionFlow) {
        this.tradeIngestionFlow = tradeIngestionFlow;
    }

    @PostMapping
    public ResponseEntity<ReconciliationJobResponse> submitReconciliationJob(
            @RequestBody ReconciliationJobRequest request) {

        log.info("Received HTTP POST /api/v1/jobs/reconciliation for source [{}] with {} record(s)",
                request.getSource(), request.getRecords() != null ? request.getRecords().size() : 0);

        // Forward through Ikasan Flow entry pipeline
        Object flowResult = tradeIngestionFlow.onConsumerEvent(request);

        if (flowResult instanceof ReconciliationJobResponse response) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        throw new IllegalStateException("Flow output was not of type ReconciliationJobResponse: " + flowResult);
    }
}
