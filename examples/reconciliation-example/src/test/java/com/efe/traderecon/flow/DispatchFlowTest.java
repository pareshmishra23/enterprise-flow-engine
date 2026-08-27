package com.efe.traderecon.flow;

import com.efe.traderecon.domain.Task;
import com.efe.traderecon.domain.TaskStatus;
import com.efe.traderecon.flow.dispatch.ScheduledTriggerEvent;
import com.efe.traderecon.ikasan.model.IkasanFlow;
import com.efe.traderecon.messaging.inmemory.InMemoryQueue;
import com.efe.traderecon.messaging.spi.MessagingConsumer;
import com.efe.traderecon.messaging.spi.MessagingMessage;
import com.efe.traderecon.persistence.spi.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class DispatchFlowTest {

    @Autowired
    @Qualifier("reconciliationDispatchFlow")
    private IkasanFlow dispatchFlow;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private InMemoryQueue inMemoryQueue;

    @Autowired
    private MessagingConsumer<Task> messagingConsumer;

    @BeforeEach
    void setUp() {
        taskRepository.clear();
        inMemoryQueue.clearAll();
    }

    @Test
    @DisplayName("Should retrieve pending task, mark as DISPATCHED, and produce to messaging queue")
    void shouldDispatchPendingTask() {
        Task pendingTask = new Task("TSK-DISP-01", "JOB-DISP-01", "TRADE_RECONCILIATION");
        pendingTask.setStatus(TaskStatus.PENDING);
        taskRepository.save(pendingTask);

        // Execute dispatch flow on consumer trigger
        ScheduledTriggerEvent trigger = new ScheduledTriggerEvent("test-trigger", Instant.now());
        dispatchFlow.onConsumerEvent(trigger);

        // Verify task state updated in repository
        Optional<Task> updatedTask = taskRepository.findById("TSK-DISP-01");
        assertThat(updatedTask).isPresent();
        assertThat(updatedTask.get().getStatus()).isIn(TaskStatus.DISPATCHED, TaskStatus.PROCESSING, TaskStatus.COMPLETED);
        assertThat(updatedTask.get().getAttemptCount()).isGreaterThanOrEqualTo(1);

        // Verify message enqueued on messaging SPI destination
        Optional<MessagingMessage<Task>> polledMsg = messagingConsumer.poll("trade.recon.tasks", 500, TimeUnit.MILLISECONDS);
        assertThat(polledMsg).isPresent();
        assertThat(polledMsg.get().getPayload().getTaskId()).isEqualTo("TSK-DISP-01");
    }
}
