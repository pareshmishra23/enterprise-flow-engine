package com.efe.traderecon.persistence.inmemory;

import com.efe.traderecon.domain.ReconciliationResult;
import com.efe.traderecon.persistence.spi.ResultRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryResultRepository implements ResultRepository {

    private final ConcurrentHashMap<String, ReconciliationResult> storage = new ConcurrentHashMap<>();

    @Override
    public ReconciliationResult save(ReconciliationResult result) {
        if (result == null || result.getResultId() == null) {
            throw new IllegalArgumentException("Result and resultId cannot be null");
        }
        storage.put(result.getResultId(), result);
        return result;
    }

    @Override
    public List<ReconciliationResult> saveAll(List<ReconciliationResult> results) {
        if (results == null) return new ArrayList<>();
        results.forEach(this::save);
        return results;
    }

    @Override
    public Optional<ReconciliationResult> findById(String resultId) {
        if (resultId == null) return Optional.empty();
        return Optional.ofNullable(storage.get(resultId));
    }

    @Override
    public List<ReconciliationResult> findByJobId(String jobId) {
        if (jobId == null) return new ArrayList<>();
        return storage.values().stream()
                .filter(r -> jobId.equals(r.getJobId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReconciliationResult> findByTaskId(String taskId) {
        if (taskId == null) return new ArrayList<>();
        return storage.values().stream()
                .filter(r -> taskId.equals(r.getTaskId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReconciliationResult> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void clear() {
        storage.clear();
    }
}
