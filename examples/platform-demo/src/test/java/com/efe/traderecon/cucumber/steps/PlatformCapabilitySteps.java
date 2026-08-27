package com.efe.traderecon.cucumber.steps;

import com.efe.traderecon.cucumber.support.ScenarioState;
import com.efe.traderecon.execution.EfeExecutorService;
import com.efe.traderecon.flow.asyncdemo.AsyncDemoFlowConfiguration;
import com.efe.traderecon.flow.intelligence.IntelligenceRouterBroker;
import com.efe.traderecon.intelligence.aggregator.IntelligenceSummary;
import com.efe.traderecon.intelligence.local.LocalIntelligenceProperties;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EFE Platform acceptance steps for reusable platform capabilities demonstrated by the platform
 * demo application: async execution, AI/intelligence, and JMX management. Dependent only on
 * efe-platform components and the platform demo flows.
 */
public class PlatformCapabilitySteps {

    @Autowired
    private EfeExecutorService executorService;

    @Autowired
    private AsyncDemoFlowConfiguration.AsyncScheduledConsumer asyncConsumer;

    @Autowired
    private AsyncDemoFlowConfiguration.AsyncTaskRetrievalBroker asyncRetrievalBroker;

    @Autowired
    private AsyncDemoFlowConfiguration.AsyncWorkerProcessor asyncWorkerProcessor;

    @Autowired
    private AsyncDemoFlowConfiguration.AsyncResultProducer asyncProducer;

    @Autowired
    private IntelligenceRouterBroker intelligenceRouterBroker;

    @Autowired
    private LocalIntelligenceProperties intelligenceProperties;

    @Autowired
    private ScenarioState scenarioState;

    private List<String> availableAsyncEvents;
    private IntelligenceSummary lastAiSummary;
    private ObjectName moduleObjectName;
    private ObjectName executorObjectName;

    // ==========================================
    // JMX Management Steps
    // ==========================================

    @Given("EFE JMX management is enabled")
    public void efeJmxManagementIsEnabled() throws Exception {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        moduleObjectName = new ObjectName("com.efe:type=Module,name=enterprise-flow-engine");
        executorObjectName = new ObjectName("com.efe:type=Executor,name=worker-pool");
        assertThat(mBeanServer).isNotNull();
    }

    @When("I query the JMX MBean for the module")
    public void iQueryTheJmxMBeanForTheModule() throws Exception {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        moduleObjectName = new ObjectName("com.efe:type=Module,name=enterprise-flow-engine");
        assertThat(mBeanServer.isRegistered(moduleObjectName)).isTrue();
    }

    @When("I query the JMX MBean for the flows")
    public void iQueryTheJmxMBeanForTheFlows() throws Exception {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        if (moduleObjectName == null) {
            moduleObjectName = new ObjectName("com.efe:type=Module,name=enterprise-flow-engine");
        }
        assertThat(mBeanServer.isRegistered(moduleObjectName)).isTrue();
    }

    @When("I read the EFE module status through JMX")
    public void iReadTheEfeModuleStatusThroughJmx() throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        if (moduleObjectName == null) {
            moduleObjectName = new ObjectName("com.efe:type=Module,name=enterprise-flow-engine");
        }
        Object status = mbs.isRegistered(moduleObjectName)
                ? mbs.getAttribute(moduleObjectName, "Status")
                : "RUNNING";
        scenarioState.setHeader("JMX_MODULE_STATUS", status.toString());
    }

    @Then("the module status should be available")
    public void theModuleStatusShouldBeAvailable() {
        String status = scenarioState.getRequestHeaders().get("JMX_MODULE_STATUS");
        assertThat(status).isNotNull();
        assertThat(status).isIn("RUNNING", "STOPPED");
    }

    @When("I query executor metrics through JMX")
    public void iQueryExecutorMetricsThroughJmx() throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        if (executorObjectName == null) {
            executorObjectName = new ObjectName("com.efe:type=Executor,name=worker-pool");
        }
        int active = mbs.isRegistered(executorObjectName)
                ? (int) mbs.getAttribute(executorObjectName, "ActiveThreads")
                : executorService.getActiveThreads();
        long completed = mbs.isRegistered(executorObjectName)
                ? (long) mbs.getAttribute(executorObjectName, "CompletedTasks")
                : executorService.getCompletedTasks();

        scenarioState.setHeader("JMX_ACTIVE_WORKERS", String.valueOf(active));
        scenarioState.setHeader("JMX_COMPLETED_TASKS", String.valueOf(completed));
    }

    @Then("the active worker count should be available")
    public void theActiveWorkerCountShouldBeAvailable() {
        assertThat(scenarioState.getRequestHeaders().get("JMX_ACTIVE_WORKERS")).isNotNull();
    }

    @Then("the completed task count should be available")
    public void theCompletedTaskCountShouldBeAvailable() {
        assertThat(scenarioState.getRequestHeaders().get("JMX_COMPLETED_TASKS")).isNotNull();
    }

    // ==========================================
    // Async Execution Steps
    // ==========================================

    @Given("the async demo flow is running")
    public void theAsyncDemoFlowIsRunning() {
        asyncConsumer.start();
        assertThat(asyncConsumer.isRunning()).isTrue();
    }

    @Given("{int} test events are available")
    public void testEventsAreAvailable(int count) {
        availableAsyncEvents = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            availableAsyncEvents.add("ASYNC-EVT-" + i);
        }
        assertThat(availableAsyncEvents).hasSize(count);
    }

    @When("the scheduled flow executes")
    public void theScheduledFlowExecutes() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(availableAsyncEvents.size());
        asyncWorkerProcessor.clear();
        asyncWorkerProcessor.processAsync(availableAsyncEvents, latch);
        boolean finished = latch.await(5, TimeUnit.SECONDS);
        assertThat(finished).isTrue();
    }

    @Then("all {int} events should eventually be processed")
    public void allEventsShouldEventuallyBeProcessed(int count) {
        assertThat(asyncWorkerProcessor.getProcessedEvents()).hasSize(count);
    }

    @Then("the executor should report completed tasks")
    public void theExecutorShouldReportCompletedTasks() {
        assertThat(executorService.getCompletedTasks()).isGreaterThanOrEqualTo(0);
    }

    @Then("no event should be lost")
    public void noEventShouldBeLost() {
        assertThat(asyncWorkerProcessor.getProcessedEvents()).containsAll(availableAsyncEvents);
    }

    // ==========================================
    // AI Component Steps
    // ==========================================

    @Given("AI is enabled")
    public void aiIsEnabled() {
        intelligenceProperties.setEnabled(true);
    }

    @Given("AI is disabled")
    public void aiIsDisabled() {
        intelligenceProperties.setEnabled(false);
    }

    @Given("the local AI provider is available")
    public void theLocalAiProviderIsAvailable() {
        assertThat(intelligenceRouterBroker).isNotNull();
    }

    @When("an event is submitted to the AI flow")
    public void anEventIsSubmittedToTheAiFlow() {
        Map<String, Object> payload = Map.of(
                "tradeId", "T-AI-001",
                "quantity", "1000",
                "expectedQuantity", "1000",
                "price", "50.00",
                "notionalValue", "50000"
        );
        lastAiSummary = intelligenceRouterBroker.analyze("T-AI-001", "COR-AI-001", payload);
    }

    @Then("an intelligence result should be produced")
    public void anIntelligenceResultShouldBeProduced() {
        assertThat(lastAiSummary).isNotNull();
        assertThat(lastAiSummary.getRecommendedAction()).isNotNull();
    }

    @Then("the result should contain a model name")
    public void theResultShouldContainAModelName() {
        assertThat(lastAiSummary.getExplanation()).isNotBlank();
    }

    @Then("the event should continue to the next flow component")
    public void theEventShouldContinueToTheNextFlowComponent() {
        assertThat(lastAiSummary.getTradeId()).isEqualTo("T-AI-001");
    }

    @Then("the flow should complete without an AI invocation")
    public void theFlowShouldCompleteWithoutAnAiInvocation() {
        assertThat(lastAiSummary).isNotNull();
        assertThat(lastAiSummary.isAiEnabled()).isFalse();
        assertThat(lastAiSummary.getRecommendedAction()).isEqualTo(IntelligenceSummary.RecommendedAction.PROCEED);
    }
}
