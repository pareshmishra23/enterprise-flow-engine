package com.efe.traderecon.persistence;

import com.efe.traderecon.domain.*;
import com.efe.traderecon.persistence.inmemory.InMemoryJobRepository;
import com.efe.traderecon.persistence.inmemory.InMemoryResultRepository;
import com.efe.traderecon.persistence.inmemory.InMemoryTaskRepository;
import com.efe.traderecon.persistence.inmemory.InMemoryTradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class InMemoryRepositoriesTest {

    private InMemoryJobRepository jobRepository;
    private InMemoryTaskRepository taskRepository;
    private InMemoryTradeRepository tradeRepository;
    private InMemoryResultRepository resultRepository;

    @BeforeEach
    void setUp() {
        jobRepository = new InMemoryJobRepository();
        taskRepository = new InMemoryTaskRepository();
        tradeRepository = new InMemoryTradeRepository();
        resultRepository = new InMemoryResultRepository();
    }

    @Test
    @DisplayName("Should perform CRUD and status filtering across all in-memory repositories")
    void shouldPerformRepositoryOperations() {
        // 1. Job Repository
        Job job = new Job("JOB-100", JobType.RECONCILIATION, "CUSTODIAN", LocalDate.now(), 5);
        jobRepository.save(job);
        assertThat(jobRepository.findById("JOB-100")).isPresent();
        assertThat(jobRepository.findByStatus(JobStatus.SUBMITTED)).hasSize(1);

        // 2. Task Repository
        Task task = new Task("TSK-100", "JOB-100", "TRADE_RECONCILIATION");
        taskRepository.save(task);
        assertThat(taskRepository.findById("TSK-100")).isPresent();
        assertThat(taskRepository.findByJobId("JOB-100")).hasSize(1);
        assertThat(taskRepository.findByStatus(TaskStatus.PENDING)).hasSize(1);

        // 3. Trade Repository
        Trade trade = new Trade("TR-1", "JOB-100", "ACC-1", "SEC-1", new BigDecimal("100"), new BigDecimal("50"));
        tradeRepository.save(trade);
        assertThat(tradeRepository.findById("TR-1", "JOB-100")).isPresent();
        assertThat(tradeRepository.findByJobId("JOB-100")).hasSize(1);

        // 4. Result Repository
        ReconciliationResult res = new ReconciliationResult("RES-1", "JOB-100", "TSK-100", "TR-1", DifferenceType.MATCH, BigDecimal.ZERO);
        resultRepository.save(res);
        assertThat(resultRepository.findById("RES-1")).isPresent();
        assertThat(resultRepository.findByJobId("JOB-100")).hasSize(1);
        assertThat(resultRepository.findByTaskId("TSK-100")).hasSize(1);
    }
}
