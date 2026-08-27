package com.efe.traderecon.flow.processing;

import com.efe.traderecon.domain.Task;
import com.efe.traderecon.domain.TaskStatus;
import com.efe.traderecon.ikasan.model.IkasanConverter;
import com.efe.traderecon.messaging.spi.MessagingMessage;
import com.efe.traderecon.persistence.spi.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TaskEventTranslator implements IkasanConverter<MessagingMessage<Task>, Task> {
    private static final Logger log = LoggerFactory.getLogger(TaskEventTranslator.class);

    private final TaskRepository taskRepository;

    public TaskEventTranslator(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public String getName() {
        return "task-event-translator";
    }

    @Override
    public Task convert(MessagingMessage<Task> message) {
        if (message == null || message.getPayload() == null) {
            throw new IllegalArgumentException("Received empty message or payload in processing flow");
        }
        Task task = message.getPayload();
        task.setStatus(TaskStatus.PROCESSING);
        task.setStartedAt(Instant.now());
        taskRepository.save(task);

        log.debug("Extracted and started processing for Task [{}] (job: {})", task.getTaskId(), task.getJobId());
        return task;
    }
}
