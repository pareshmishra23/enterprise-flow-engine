package com.efe.traderecon.flow;

import com.efe.traderecon.api.dto.ReconciliationJobRequest;
import com.efe.traderecon.api.dto.ReconciliationJobResponse;
import com.efe.traderecon.api.dto.TradeRecordDto;
import com.efe.traderecon.domain.Job;
import com.efe.traderecon.domain.JobStatus;
import com.efe.traderecon.domain.Task;
import com.efe.traderecon.domain.TaskStatus;
import com.efe.traderecon.ikasan.model.IkasanFlow;
import com.efe.traderecon.persistence.spi.JobRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class IngestionFlowTest {

    @Autowired
    @Qualifier("tradeIngestionFlow")
    private IkasanFlow tradeIngestionFlow;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @BeforeEach
    void setUp() {
        jobRepository.clear();
        taskRepository.clear();
        tradeRepository.clear();
    }

    @Test
    @DisplayName("Should ingest valid reconciliation job, register in persistence, and produce response")
    void shouldIngestValidJob() {
        ReconciliationJobRequest request = new ReconciliationJobRequest(
                LocalDate.of(2026, 8, 27),
                "CUSTODIAN",
                List.of(
                        new TradeRecordDto("TR-001", "ACC-01", "SEC-1", new BigDecimal("100.0"), new BigDecimal("50.0")),
                        new TradeRecordDto("TR-002", "ACC-02", "SEC-2", new BigDecimal("200.0"), new BigDecimal("75.0"))
                )
        );

        Object result = tradeIngestionFlow.onConsumerEvent(request);

        assertThat(result).isInstanceOf(ReconciliationJobResponse.class);
        ReconciliationJobResponse response = (ReconciliationJobResponse) result;

        assertThat(response.getJobId()).isNotNull().startsWith("JOB-");
        assertThat(response.getStatus()).isEqualTo(JobStatus.SUBMITTED);
        assertThat(response.getSource()).isEqualTo("CUSTODIAN");
        assertThat(response.getTotalRecords()).isEqualTo(2);

        // Verify Job in repository
        Optional<Job> savedJob = jobRepository.findById(response.getJobId());
        assertThat(savedJob).isPresent();
        assertThat(savedJob.get().getStatus()).isEqualTo(JobStatus.SUBMITTED);
        assertThat(savedJob.get().getTotalRecords()).isEqualTo(2);

        // Verify pending Task created
        List<Task> tasks = taskRepository.findByJobId(response.getJobId());
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getStatus()).isEqualTo(TaskStatus.PENDING);

        // Verify Trades persisted
        assertThat(tradeRepository.findByJobId(response.getJobId())).hasSize(2);
    }

    @Test
    @DisplayName("Should fail validation when required fields are missing")
    void shouldFailValidationWhenFieldMissing() {
        ReconciliationJobRequest invalidRequest = new ReconciliationJobRequest(
                null, // Missing businessDate
                "CUSTODIAN",
                List.of()
        );

        assertThatThrownBy(() -> tradeIngestionFlow.onConsumerEvent(invalidRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("businessDate");
    }
}
