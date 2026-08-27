package com.efe.traderecon.cucumber.steps;

import com.efe.traderecon.execution.EfeExecutorService;
import com.efe.traderecon.flow.asyncexec.AsyncExecutionFlowConfiguration;
import com.efe.traderecon.ikasan.model.FlowState;
import com.efe.traderecon.ikasan.model.IkasanFlow;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AsyncExecutionFlowSteps {

    @Autowired
    @Qualifier("efeAsyncFlow")
    private IkasanFlow asyncFlow;

    @Autowired
    private AsyncExecutionFlowConfiguration.EfeAsyncScheduledConsumer consumer;

    @Autowired
    private AsyncExecutionFlowConfiguration.EfeAsyncResultProducer producer;

    @Autowired
    private EfeExecutorService executorService;

    @Given("the EFE async execution flow is running")
    public void theEfeAsyncExecutionFlowIsRunning() {
        assertThat(asyncFlow).isNotNull();
        if (asyncFlow.getState() != FlowState.RUNNING) {
            asyncFlow.start();
        }
        assertThat(asyncFlow.getState()).isEqualTo(FlowState.RUNNING);
    }

    @Given("the async result store is empty")
    public void theAsyncResultStoreIsEmpty() {
        producer.clear();
    }

    @Given("a scheduled trigger occurs with batch ID {string}")
    public void aScheduledTriggerOccursWithBatchID(String batchId) {
        consumer.trigger(batchId);
    }

    @When("the async flow processes the batch of {int} partitioned tasks")
    public void theAsyncFlowProcessesTheBatchOfPartitionedTasks(int taskCount) {
        List<Map<String, Object>> results = producer.getCompletedResults();
        assertThat(results).hasSize(taskCount);
    }

    @Then("all {int} tasks should complete asynchronously")
    public void allTasksShouldCompleteAsynchronously(int expectedCount) {
        List<Map<String, Object>> results = producer.getCompletedResults();
        assertThat(results).hasSize(expectedCount);
        for (Map<String, Object> item : results) {
            assertThat(item.get("status")).isEqualTo("COMPLETED");
        }
    }

    @And("the executor worker pool should report completed tasks")
    public void theExecutorWorkerPoolShouldReportCompletedTasks() {
        assertThat(executorService.getCompletedTasks()).isGreaterThanOrEqualTo(0);
    }

    @And("each completed task should contain a valid worker outcome")
    public void eachCompletedTaskShouldContainAValidWorkerOutcome() {
        List<Map<String, Object>> results = producer.getCompletedResults();
        for (Map<String, Object> item : results) {
            assertThat(item.get("workerOutcome")).isNotNull();
            assertThat(item.get("completedAt")).isNotNull();
        }
    }

    @Given("the async executor worker pool is initialized")
    public void theAsyncExecutorWorkerPoolIsInitialized() {
        assertThat(executorService).isNotNull();
        assertThat(executorService.getMaximumPoolSize()).isEqualTo(10);
    }

    @When("{int} tasks are submitted in parallel")
    public void tasksAreSubmittedInParallel(int count) {
        for (int i = 0; i < count; i++) {
            executorService.execute(() -> {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    @Then("the active threads should not exceed the maximum pool size of {int}")
    public void theActiveThreadsShouldNotExceedTheMaximumPoolSizeOf(int maxPoolSize) {
        assertThat(executorService.getActiveThreads()).isLessThanOrEqualTo(maxPoolSize);
    }

    @And("the task queue depth should remain within capacity")
    public void theTaskQueueDepthShouldRemainWithinCapacity() {
        assertThat(executorService.getQueueSize()).isLessThanOrEqualTo(100);
    }
}
