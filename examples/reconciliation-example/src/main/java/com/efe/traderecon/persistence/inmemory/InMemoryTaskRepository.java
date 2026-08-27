package com.efe.traderecon.persistence.inmemory;

import com.efe.traderecon.domain.Task;
import com.efe.traderecon.domain.TaskStatus;
import com.efe.traderecon.persistence.spi.TaskRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryTaskRepository implements TaskRepository {

    private final ConcurrentHashMap<String, Task> storage = new ConcurrentHashMap<>();

    @Override
    public Task save(Task task) {
        if (task == null || task.getTaskId() == null) {
            throw new IllegalArgumentException("Task and taskId cannot be null");
        }
        storage.put(task.getTaskId(), task);
        return task;
    }

    @Override
    public List<Task> saveAll(List<Task> tasks) {
        if (tasks == null) return new ArrayList<>();
        tasks.forEach(this::save);
        return tasks;
    }

    @Override
    public Optional<Task> findById(String taskId) {
        if (taskId == null) return Optional.empty();
        return Optional.ofNullable(storage.get(taskId));
    }

    @Override
    public List<Task> findByJobId(String jobId) {
        if (jobId == null) return new ArrayList<>();
        return storage.values().stream()
                .filter(t -> jobId.equals(t.getJobId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findByStatus(TaskStatus status) {
        if (status == null) return new ArrayList<>();
        return storage.values().stream()
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void clear() {
        storage.clear();
    }
}
