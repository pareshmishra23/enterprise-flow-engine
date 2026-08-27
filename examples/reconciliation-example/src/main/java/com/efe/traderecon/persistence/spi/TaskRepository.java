package com.efe.traderecon.persistence.spi;

import com.efe.traderecon.domain.Task;
import com.efe.traderecon.domain.TaskStatus;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    Task save(Task task);
    List<Task> saveAll(List<Task> tasks);
    Optional<Task> findById(String taskId);
    List<Task> findByJobId(String jobId);
    List<Task> findByStatus(TaskStatus status);
    List<Task> findAll();
    void clear();
}
