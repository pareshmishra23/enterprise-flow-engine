package com.efe.traderecon.flow.processing;

import com.efe.traderecon.domain.*;
import com.efe.traderecon.ikasan.model.IkasanBroker;
import com.efe.traderecon.persistence.spi.JobRepository;
import com.efe.traderecon.persistence.spi.ResultRepository;
import com.efe.traderecon.persistence.spi.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class ResultPersistenceBroker implements IkasanBroker<TaskResult, TaskResult> {
    private static final Logger log = LoggerFactory.getLogger(ResultPersistenceBroker.class);

    private final ResultRepository resultRepository;
    private final TaskRepository taskRepository;
    private final JobRepository jobRepository;

    public ResultPersistenceBroker(
            ResultRepository resultRepository,
            TaskRepository taskRepository,
            JobRepository jobRepository) {
        this.resultRepository = resultRepository;
        this.taskRepository = taskRepository;
        this.jobRepository = jobRepository;
    }

    @Override
    public String getName() {
        return "result-persistence-broker";
    }

    @Override
    public TaskResult invoke(TaskResult result) {
        if (result == null) return null;

        // Persist individual reconciliation results
        if (result.getResults() != null && !result.getResults().isEmpty()) {
            resultRepository.saveAll(result.getResults());
        }

        // Update task status
        Optional<Task> taskOpt = taskRepository.findById(result.getTaskId());
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setStatus(result.getStatus());
            task.setCompletedAt(Instant.now());
            taskRepository.save(task);
        }

        // Update job counters & status
        Optional<Job> jobOpt = jobRepository.findById(result.getJobId());
        if (jobOpt.isPresent()) {
            Job job = jobOpt.get();
            job.setMatchedRecords(job.getMatchedRecords() + result.getMatchedCount());
            job.setBreakRecords(job.getBreakRecords() + result.getBreakCount());
            job.setFailedRecords(job.getFailedRecords() + result.getFailureCount());
            job.setProcessedRecords(job.getMatchedRecords() + job.getBreakRecords() + job.getFailedRecords());
            job.setStatus(JobStatus.COMPLETED);
            job.setCompletedAt(Instant.now());
            jobRepository.save(job);
            log.info("Updated Job [{}] status to COMPLETED (matched: {}, breaks: {})",
                    job.getJobId(), job.getMatchedRecords(), job.getBreakRecords());
        }

        return result;
    }
}
