package com.efe.traderecon.flow.ingestion;

import com.efe.traderecon.api.dto.ReconciliationJobRequest;
import com.efe.traderecon.api.dto.ReconciliationJobResponse;
import com.efe.traderecon.domain.*;
import com.efe.traderecon.ikasan.model.IkasanBroker;
import com.efe.traderecon.persistence.spi.JobRepository;
import com.efe.traderecon.persistence.spi.TaskRepository;
import com.efe.traderecon.persistence.spi.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class JobRegistrationBroker implements IkasanBroker<ReconciliationJobRequest, ReconciliationJobResponse> {
    private static final Logger log = LoggerFactory.getLogger(JobRegistrationBroker.class);

    private final JobRepository jobRepository;
    private final TaskRepository taskRepository;
    private final TradeRepository tradeRepository;

    public JobRegistrationBroker(
            JobRepository jobRepository,
            TaskRepository taskRepository,
            TradeRepository tradeRepository) {
        this.jobRepository = jobRepository;
        this.taskRepository = taskRepository;
        this.tradeRepository = tradeRepository;
    }

    @Override
    public String getName() {
        return "job-registration-broker";
    }

    @Override
    public ReconciliationJobResponse invoke(ReconciliationJobRequest request) {
        String jobId = "JOB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        int totalRecords = request.getRecords() != null ? request.getRecords().size() : 0;

        Job job = new Job(jobId, JobType.RECONCILIATION, request.getSource(), request.getBusinessDate(), totalRecords);
        job.setStatus(JobStatus.SUBMITTED);
        job.setCreatedAt(Instant.now());
        jobRepository.save(job);

        // Persist trade records
        if (request.getRecords() != null && !request.getRecords().isEmpty()) {
            List<Trade> trades = new ArrayList<>();
            request.getRecords().forEach(r -> {
                trades.add(new Trade(r.getTradeId(), jobId, r.getAccountId(), r.getSecurityId(), r.getQuantity(), r.getPrice()));
            });
            tradeRepository.saveAll(trades);
        }

        // Create initial pending task
        String taskId = "TSK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Task initialTask = new Task(taskId, jobId, "TRADE_RECONCILIATION");
        initialTask.setStatus(TaskStatus.PENDING);
        taskRepository.save(initialTask);

        log.info("Registered Job [{}] with Task [{}] for source [{}] and {} records",
                jobId, taskId, request.getSource(), totalRecords);

        return new ReconciliationJobResponse(
                jobId,
                JobStatus.SUBMITTED,
                request.getSource(),
                request.getBusinessDate(),
                totalRecords,
                "Job registered successfully for asynchronous reconciliation",
                job.getCreatedAt()
        );
    }
}
