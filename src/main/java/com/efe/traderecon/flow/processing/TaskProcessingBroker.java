package com.efe.traderecon.flow.processing;

import com.efe.traderecon.domain.Task;
import com.efe.traderecon.domain.TaskResult;
import com.efe.traderecon.ikasan.model.IkasanBroker;
import com.efe.traderecon.processor.TaskProcessorRegistry;
import com.efe.traderecon.processor.spi.TaskProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TaskProcessingBroker implements IkasanBroker<Task, TaskResult> {
    private static final Logger log = LoggerFactory.getLogger(TaskProcessingBroker.class);

    private final TaskProcessorRegistry processorRegistry;

    public TaskProcessingBroker(TaskProcessorRegistry processorRegistry) {
        this.processorRegistry = processorRegistry;
    }

    @Override
    public String getName() {
        return "task-processing-broker";
    }

    @Override
    public TaskResult invoke(Task task) {
        log.info("TaskProcessingBroker dispatching task [{}] (type: {}) to business processor",
                task.getTaskId(), task.getTaskType());

        TaskProcessor processor = processorRegistry.getProcessor(task.getTaskType());
        return processor.process(task);
    }
}
