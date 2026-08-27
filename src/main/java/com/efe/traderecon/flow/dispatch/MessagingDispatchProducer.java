package com.efe.traderecon.flow.dispatch;

import com.efe.traderecon.domain.Task;
import com.efe.traderecon.ikasan.model.IkasanProducer;
import com.efe.traderecon.messaging.spi.MessagingMessage;
import com.efe.traderecon.messaging.spi.MessagingProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MessagingDispatchProducer implements IkasanProducer<Task> {
    private static final Logger log = LoggerFactory.getLogger(MessagingDispatchProducer.class);
    public static final String DESTINATION = "trade.recon.tasks";

    private final MessagingProducer<Task> messagingProducer;

    public MessagingDispatchProducer(MessagingProducer<Task> messagingProducer) {
        this.messagingProducer = messagingProducer;
    }

    @Override
    public String getName() {
        return "messaging-dispatch-producer";
    }

    @Override
    public void produce(Task task) {
        if (task == null) return;
        MessagingMessage<Task> message = new MessagingMessage<>(task);
        message.setHeader("jobId", task.getJobId());
        message.setHeader("taskId", task.getTaskId());
        message.setHeader("taskType", task.getTaskType());

        log.info("Dispatching task [{}] to messaging destination [{}] using provider [{}]",
                task.getTaskId(), DESTINATION, messagingProducer.getProviderName());
        messagingProducer.send(DESTINATION, message);
    }
}
