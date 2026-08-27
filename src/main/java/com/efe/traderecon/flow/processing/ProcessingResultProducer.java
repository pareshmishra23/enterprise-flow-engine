package com.efe.traderecon.flow.processing;

import com.efe.traderecon.domain.TaskResult;
import com.efe.traderecon.ikasan.model.IkasanProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProcessingResultProducer implements IkasanProducer<TaskResult> {
    private static final Logger log = LoggerFactory.getLogger(ProcessingResultProducer.class);

    @Override
    public String getName() {
        return "processing-result-producer";
    }

    @Override
    public void produce(TaskResult result) {
        if (result == null) return;
        log.info("Completed asynchronous reconciliation flow for task [{}] (job: {}): matched={}, breaks={}",
                result.getTaskId(), result.getJobId(), result.getMatchedCount(), result.getBreakCount());
    }
}
