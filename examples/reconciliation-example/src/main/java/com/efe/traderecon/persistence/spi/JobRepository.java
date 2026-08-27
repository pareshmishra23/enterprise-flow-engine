package com.efe.traderecon.persistence.spi;

import com.efe.traderecon.domain.Job;
import com.efe.traderecon.domain.JobStatus;

import java.util.List;
import java.util.Optional;

public interface JobRepository {
    Job save(Job job);
    Optional<Job> findById(String jobId);
    List<Job> findByStatus(JobStatus status);
    List<Job> findAll();
    boolean existsById(String jobId);
    void deleteById(String jobId);
    void clear();
}
