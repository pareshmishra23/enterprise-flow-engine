package com.efe.traderecon.flow.dispatch;

import com.efe.traderecon.domain.Task;
import com.efe.traderecon.domain.TaskStatus;
import com.efe.traderecon.ikasan.model.IkasanSplitter;
import com.efe.traderecon.persistence.spi.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TaskPreparationSplitter implements IkasanSplitter<List<Task>, Task> {
    private static final Logger log = LoggerFactory.getLogger(TaskPreparationSplitter.class);

    private final TaskRepository taskRepository;

    public TaskPreparationSplitter(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public String getName() {
        return "task-preparation-splitter";
    }

    @Override
    public List<Task> split(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return new ArrayList<>();
        }

        List<Task> preparedTasks = new ArrayList<>();
        for (Task task : tasks) {
            task.setStatus(TaskStatus.DISPATCHED);
            task.setAttemptCount(task.getAttemptCount() + 1);
            taskRepository.save(task);
            preparedTasks.add(task);
            log.debug("Prepared task [{}] for dispatch (attempt: {})", task.getTaskId(), task.getAttemptCount());
        }
        return preparedTasks;
    }
}
