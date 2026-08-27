package com.efe.traderecon.flow.dispatch;

import com.efe.traderecon.domain.Task;
import com.efe.traderecon.domain.TaskStatus;
import com.efe.traderecon.ikasan.model.IkasanBroker;
import com.efe.traderecon.persistence.spi.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskRetrievalBroker implements IkasanBroker<ScheduledTriggerEvent, List<Task>> {
    private static final Logger log = LoggerFactory.getLogger(TaskRetrievalBroker.class);

    private final TaskRepository taskRepository;

    public TaskRetrievalBroker(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public String getName() {
        return "task-retrieval-broker";
    }

    @Override
    public List<Task> invoke(ScheduledTriggerEvent trigger) {
        List<Task> pendingTasks = taskRepository.findByStatus(TaskStatus.PENDING);
        if (!pendingTasks.isEmpty()) {
            log.info("TaskRetrievalBroker found {} pending task(s) ready for dispatch", pendingTasks.size());
        }
        return pendingTasks;
    }
}
