package com.efe.traderecon.flow;

import com.efe.traderecon.domain.*;
import com.efe.traderecon.ikasan.model.IkasanFlow;
import com.efe.traderecon.messaging.spi.MessagingMessage;
import com.efe.traderecon.persistence.spi.JobRepository;
import com.efe.traderecon.persistence.spi.ResultRepository;
import com.efe.traderecon.persistence.spi.TaskRepository;
import com.efe.traderecon.persistence.spi.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ProcessingFlowTest {

    @Autowired
    @Qualifier("reconciliationProcessingFlow")
    private IkasanFlow processingFlow;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private ResultRepository resultRepository;

    @BeforeEach
    void setUp() {
        jobRepository.clear();
        taskRepository.clear();
        tradeRepository.clear();
        resultRepository.clear();
    }

    @Test
    @DisplayName("Should process task message, execute reconciliation, and persist results")
    void shouldProcessTaskMessage() {
        String jobId = "JOB-PROC-01";
        String taskId = "TSK-PROC-01";

        Job job = new Job(jobId, JobType.RECONCILIATION, "CUSTODIAN", LocalDate.now(), 2);
        job.setStatus(JobStatus.SUBMITTED);
        jobRepository.save(job);

        Task task = new Task(taskId, jobId, "TRADE_RECONCILIATION");
        task.setStatus(TaskStatus.DISPATCHED);
        taskRepository.save(task);

        Trade t1 = new Trade("TR-P1", jobId, "ACC-1", "AAPL", new BigDecimal("100"), new BigDecimal("150.0"));
        Trade t2 = new Trade("TR-P2", jobId, "ACC-2", "GOOGL", new BigDecimal("0"), new BigDecimal("100.0")); // Will break due to 0 quantity
        tradeRepository.saveAll(List.of(t1, t2));

        MessagingMessage<Task> message = new MessagingMessage<>(task);

        // Send through processing flow
        Object result = processingFlow.onConsumerEvent(message);

        assertThat(result).isInstanceOf(TaskResult.class);
        TaskResult taskResult = (TaskResult) result;
        assertThat(taskResult.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(taskResult.getMatchedCount()).isEqualTo(1);
        assertThat(taskResult.getBreakCount()).isEqualTo(1);

        // Verify task updated to COMPLETED
        Optional<Task> updatedTask = taskRepository.findById(taskId);
        assertThat(updatedTask).isPresent();
        assertThat(updatedTask.get().getStatus()).isEqualTo(TaskStatus.COMPLETED);

        // Verify job updated to COMPLETED with counts
        Optional<Job> updatedJob = jobRepository.findById(jobId);
        assertThat(updatedJob).isPresent();
        assertThat(updatedJob.get().getStatus()).isEqualTo(JobStatus.COMPLETED);
        assertThat(updatedJob.get().getMatchedRecords()).isEqualTo(1);
        assertThat(updatedJob.get().getBreakRecords()).isEqualTo(1);

        // Verify results persisted in ResultRepository
        List<ReconciliationResult> results = resultRepository.findByJobId(jobId);
        assertThat(results).hasSize(2);
    }
}
