package com.efe.traderecon.persistence.inmemory;

import com.efe.traderecon.domain.Job;
import com.efe.traderecon.domain.JobStatus;
import com.efe.traderecon.persistence.spi.JobRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryJobRepository implements JobRepository {

    private final ConcurrentHashMap<String, Job> storage = new ConcurrentHashMap<>();

    @Override
    public Job save(Job job) {
        if (job == null || job.getJobId() == null) {
            throw new IllegalArgumentException("Job and jobId cannot be null");
        }
        storage.put(job.getJobId(), job);
        return job;
    }

    @Override
    public Optional<Job> findById(String jobId) {
        if (jobId == null) return Optional.empty();
        return Optional.ofNullable(storage.get(jobId));
    }

    @Override
    public List<Job> findByStatus(JobStatus status) {
        if (status == null) return new ArrayList<>();
        return storage.values().stream()
                .filter(j -> j.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<Job> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean existsById(String jobId) {
        return jobId != null && storage.containsKey(jobId);
    }

    @Override
    public void deleteById(String jobId) {
        if (jobId != null) {
            storage.remove(jobId);
        }
    }

    @Override
    public void clear() {
        storage.clear();
    }
}
