package com.efe.traderecon.persistence.spi;

import com.efe.traderecon.domain.ReconciliationResult;

import java.util.List;
import java.util.Optional;

public interface ResultRepository {
    ReconciliationResult save(ReconciliationResult result);
    List<ReconciliationResult> saveAll(List<ReconciliationResult> results);
    Optional<ReconciliationResult> findById(String resultId);
    List<ReconciliationResult> findByJobId(String jobId);
    List<ReconciliationResult> findByTaskId(String taskId);
    List<ReconciliationResult> findAll();
    void clear();
}
