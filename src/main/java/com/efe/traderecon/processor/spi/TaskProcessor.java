package com.efe.traderecon.processor.spi;

import com.efe.traderecon.domain.Task;
import com.efe.traderecon.domain.TaskResult;

public interface TaskProcessor {

    boolean supports(String taskType);

    TaskResult process(Task task);
}
