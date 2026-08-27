package com.efe.traderecon.api.controller;

import com.efe.traderecon.api.dto.JobStatusResponse;
import com.efe.traderecon.api.dto.ReconciliationJobRequest;
import com.efe.traderecon.api.dto.ReconciliationJobResponse;
import com.efe.traderecon.ikasan.model.IkasanFlow;
import com.efe.traderecon.persistence.spi.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/jobs")
public class ReconciliationJobController {
    private static final Logger log = LoggerFactory.getLogger(ReconciliationJobController.class);

    private final IkasanFlow tradeIngestionFlow;
    private final JobRepository jobRepository;

    public ReconciliationJobController(
            @Qualifier("tradeIngestionFlow") IkasanFlow tradeIngestionFlow,
            JobRepository jobRepository) {
        this.tradeIngestionFlow = tradeIngestionFlow;
        this.jobRepository = jobRepository;
    }

    @PostMapping("/reconciliation")
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

    @GetMapping("/{jobId}")
    public ResponseEntity<JobStatusResponse> getJobStatus(@PathVariable String jobId) {
        return jobRepository.findById(jobId)
                .map(JobStatusResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<JobStatusResponse>> listAllJobs() {
        List<JobStatusResponse> list = jobRepository.findAll().stream()
                .map(JobStatusResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
}
