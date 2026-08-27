package com.efe.traderecon.processor;

import com.efe.traderecon.processor.spi.TaskProcessor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TaskProcessorRegistry {

    private final List<TaskProcessor> processors;

    public TaskProcessorRegistry(List<TaskProcessor> processors) {
        this.processors = processors;
    }

    public TaskProcessor getProcessor(String taskType) {
        return processors.stream()
                .filter(p -> p.supports(taskType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No TaskProcessor registered for taskType: " + taskType));
    }

    public List<TaskProcessor> getAllProcessors() {
        return processors;
    }
}
