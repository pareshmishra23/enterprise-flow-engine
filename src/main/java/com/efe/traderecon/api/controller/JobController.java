package com.efe.traderecon.api.controller;

import com.efe.traderecon.api.dto.*;
import com.efe.traderecon.api.exception.ResourceNotFoundException;
import com.efe.traderecon.api.exception.ValidationException;
import com.efe.traderecon.api.idempotency.IdempotencyStore;
import com.efe.traderecon.domain.*;
import com.efe.traderecon.persistence.spi.JobRepository;
import com.efe.traderecon.persistence.spi.ResultRepository;
import com.efe.traderecon.persistence.spi.TaskRepository;
import com.efe.traderecon.persistence.spi.TradeRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {
    private static final Logger log = LoggerFactory.getLogger(JobController.class);

    private final JobRepository jobRepository;
    private final TaskRepository taskRepository;
    private final TradeRepository tradeRepository;
    private final ResultRepository resultRepository;
    private final IdempotencyStore idempotencyStore;

    public JobController(
            JobRepository jobRepository,
            TaskRepository taskRepository,
            TradeRepository tradeRepository,
            ResultRepository resultRepository,
            IdempotencyStore idempotencyStore) {
        this.jobRepository = jobRepository;
        this.taskRepository = taskRepository;
        this.tradeRepository = tradeRepository;
        this.resultRepository = resultRepository;
        this.idempotencyStore = idempotencyStore;
    }

    @PostMapping
    public ResponseEntity<JobSubmissionResponse> submitJob(
            @RequestBody(required = false) JobSubmissionRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
            HttpServletResponse servletResponse) {

        log.info("Received POST /api/v1/jobs with correlationId [{}] and idempotencyKey [{}]",
                correlationId, idempotencyKey);

        // Validation
        if (request == null) {
            throw new ValidationException("Request body cannot be empty", "EFE-VAL-001");
        }
        if (request.getJobType() == null || request.getJobType().isBlank()) {
            throw new ValidationException("jobType is required", "EFE-VAL-001");
        }
        if (request.getBusinessDate() == null) {
            throw new ValidationException("businessDate is required", "EFE-VAL-002");
        }

        // Idempotency check
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<JobSubmissionResponse> cached = idempotencyStore.get(idempotencyKey);
            if (cached.isPresent()) {
                JobSubmissionResponse resp = cached.get();
                log.info("Idempotent request matched for key [{}], returning existing jobId [{}]",
                        idempotencyKey, resp.getJobId());
                return ResponseEntity.status(HttpStatus.CREATED)
                        .header(HttpHeaders.LOCATION, "/api/v1/jobs/" + resp.getJobId())
                        .body(resp);
            }
        }

        // Generate Job
        String jobId = "JOB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        JobType jobTypeEnum = JobType.RECONCILIATION;
        try {
            jobTypeEnum = JobType.valueOf(request.getJobType().trim().toUpperCase());
        } catch (Exception ignored) {
        }

        int recordCount = 0;
        if (request.getPayload() != null && request.getPayload().get("records") instanceof List<?> list) {
            recordCount = list.size();
        }

        Job job = new Job(jobId, jobTypeEnum, request.getSource() != null ? request.getSource() : "DEFAULT", request.getBusinessDate(), recordCount);
        job.setStatus(JobStatus.SUBMITTED);
        job.setCreatedAt(Instant.now());
        jobRepository.save(job);

        // Extract and save trades if present
        if (request.getPayload() != null && request.getPayload().get("records") instanceof List<?> list) {
            List<Trade> trades = new ArrayList<>();
            for (Object obj : list) {
                if (obj instanceof Map<?, ?> map) {
                    String tradeId = Objects.toString(map.get("tradeId"), UUID.randomUUID().toString());
                    String accountId = Objects.toString(map.get("accountId"), "ACC-DEFAULT");
                    String securityId = Objects.toString(map.get("securityId"), "SEC-DEFAULT");
                    BigDecimal quantity = map.get("quantity") != null ? new BigDecimal(map.get("quantity").toString()) : BigDecimal.ZERO;
                    BigDecimal price = map.get("price") != null ? new BigDecimal(map.get("price").toString()) : BigDecimal.ZERO;
                    trades.add(new Trade(tradeId, jobId, accountId, securityId, quantity, price));
                }
            }
            if (!trades.isEmpty()) {
                tradeRepository.saveAll(trades);
            }
        }

        // Create initial pending task
        String taskId = "TSK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Task initialTask = new Task(taskId, jobId, request.getJobType().trim().toUpperCase());
        initialTask.setStatus(TaskStatus.PENDING);
        taskRepository.save(initialTask);

        JobSubmissionResponse response = new JobSubmissionResponse(
                jobId,
                "REGISTERED",
                request.getJobType().trim().toUpperCase(),
                job.getCreatedAt()
        );

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyStore.put(idempotencyKey, response);
        }

        return ResponseEntity.created(URI.create("/api/v1/jobs/" + jobId))
                .header(HttpHeaders.LOCATION, "/api/v1/jobs/" + jobId)
                .body(response);
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobStatusResponse> getJobStatus(@PathVariable String jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId, "EFE-JOB-404"));
        return ResponseEntity.ok(JobStatusResponse.fromDomain(job));
    }

    @GetMapping("/{jobId}/tasks")
    public ResponseEntity<TaskPageResponse> getJobTasks(
            @PathVariable String jobId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        List<Task> allTasks = taskRepository.findByJobId(jobId);
        List<TaskSummaryResponse> summaryList = allTasks.stream()
                .map(t -> new TaskSummaryResponse(t.getTaskId(), t.getStatus().name(), t.getAttemptCount()))
                .collect(Collectors.toList());

        TaskPageResponse pageResp = new TaskPageResponse(
                jobId,
                page,
                size,
                allTasks.size(),
                summaryList
        );
        return ResponseEntity.ok(pageResp);
    }

    @GetMapping("/{jobId}/results")
    public ResponseEntity<ResultPageResponse> getJobResults(
            @PathVariable String jobId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {

        List<ReconciliationResult> results = resultRepository.findByJobId(jobId);
        List<Object> rawResults = new ArrayList<>(results);

        ResultPageResponse pageResp = new ResultPageResponse(
                jobId,
                page,
                size,
                results.size(),
                rawResults
        );
        return ResponseEntity.ok(pageResp);
    }
}
