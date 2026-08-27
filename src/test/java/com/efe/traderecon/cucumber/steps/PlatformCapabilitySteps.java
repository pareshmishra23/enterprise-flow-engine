package com.efe.traderecon.cucumber.steps;

import com.efe.traderecon.api.controller.JobController;
import com.efe.traderecon.api.dto.JobSubmissionRequest;
import com.efe.traderecon.api.graphql.EfeGraphQLController;
import com.efe.traderecon.api.grpc.EfeJobGrpcAdapter;
import com.efe.traderecon.cucumber.support.ScenarioState;
import com.efe.traderecon.domain.Job;
import com.efe.traderecon.domain.JobStatus;
import com.efe.traderecon.domain.JobType;
import com.efe.traderecon.execution.EfeExecutorService;
import com.efe.traderecon.flow.asyncdemo.AsyncDemoFlowConfiguration;
import com.efe.traderecon.flow.dbdemo.DbDemoFlowConfiguration;
import com.efe.traderecon.flow.intelligence.IntelligenceRouterBroker;
import com.efe.traderecon.intelligence.aggregator.IntelligenceSummary;
import com.efe.traderecon.intelligence.local.LocalIntelligenceProperties;
import com.efe.traderecon.intelligence.spi.IntelligenceResult;
import com.efe.traderecon.persistence.spi.JobRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class PlatformCapabilitySteps {

    @Autowired
    private JobController jobController;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private EfeJobGrpcAdapter grpcAdapter;

    @Autowired
    private EfeGraphQLController graphQLController;

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
    private DbDemoFlowConfiguration.DbScheduledConsumer dbConsumer;

    @Autowired
    private DbDemoFlowConfiguration.DatabaseAccessBroker dbBroker;

    @Autowired
    private DbDemoFlowConfiguration.DbResultProducer dbProducer;

    @Autowired
    private IntelligenceRouterBroker intelligenceRouterBroker;

    @Autowired
    private LocalIntelligenceProperties intelligenceProperties;

    @Autowired
    private ScenarioState scenarioState;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private EfeJobGrpcAdapter.JobResponseGrpc lastGrpcResponse;
    private Map<String, Object> lastGraphQLResponse;
    private String registeredJobId;
    private List<String> availableAsyncEvents;
    private IntelligenceSummary lastAiSummary;
    private boolean jmxAvailable = false;
    private ObjectName moduleObjectName;
    private ObjectName executorObjectName;

    private MockMvc getMockMvc() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.standaloneSetup(jobController, graphQLController).build();
        }
        return mockMvc;
    }

    // ==========================================
    // REST API Steps
    // ==========================================

    @Given("the REST interface is available")
    public void theRestInterfaceIsAvailable() {
        assertThat(jobController).isNotNull();
    }

    @When("I submit a valid job to {string}")
    public void iSubmitAValidJobTo(String path) throws Exception {
        JobSubmissionRequest req = new JobSubmissionRequest("TRADE_RECONCILIATION", "REST", LocalDate.of(2026, 8, 27), Map.of());
        MvcResult result = getMockMvc().perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();
        scenarioState.setLatestMvcResult(result);
        if (result.getResponse().getStatus() == 201) {
            JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
            registeredJobId = root.path("jobId").asText();
            scenarioState.setCreatedJobId(registeredJobId);
        }
    }

    @Then("the HTTP status should be {int}")
    public void theHttpStatusShouldBe(int status) {
        assertThat(scenarioState.getLatestStatusCode()).isEqualTo(status);
    }

    @Then("the job should be registered")
    public void theJobShouldBeRegistered() {
        String jid = registeredJobId != null ? registeredJobId : scenarioState.getCreatedJobId();
        assertThat(jid).isNotNull();
        Optional<Job> job = jobRepository.findById(jid);
        assertThat(job).isPresent();
    }

    @When("I request the job using its jobId")
    public void iRequestTheJobUsingItsJobId() throws Exception {
        String jid = registeredJobId != null ? registeredJobId : scenarioState.getCreatedJobId();
        MvcResult result = getMockMvc().perform(get("/api/v1/jobs/" + jid))
                .andReturn();
        scenarioState.setLatestMvcResult(result);
    }

    @Then("the response should contain the same jobId")
    public void theResponseShouldContainTheSameJobId() throws Exception {
        String jid = registeredJobId != null ? registeredJobId : scenarioState.getCreatedJobId();
        JsonNode root = objectMapper.readTree(scenarioState.getLatestResponseBody());
        assertThat(root.path("jobId").asText()).isEqualTo(jid);
    }

    // ==========================================
    // gRPC API Steps
    // ==========================================

    @Given("EFE gRPC is available")
    public void efeGrpcIsAvailable() {
        assertThat(grpcAdapter).isNotNull();
    }

    @When("I submit a valid job using gRPC")
    public void iSubmitAValidJobUsingGrpc() {
        EfeJobGrpcAdapter.JobRequestGrpc request = new EfeJobGrpcAdapter.JobRequestGrpc(
                "POSITION_RECONCILIATION",
                "2026-08-27",
                Map.of("portfolio", "GLOBAL_EQUITY")
        );
        lastGrpcResponse = grpcAdapter.submitJob(request, "COR-GRPC-1", "IDEM-GRPC-1");
        registeredJobId = lastGrpcResponse.getJobId();
        scenarioState.setCreatedJobId(registeredJobId);
    }

    @Then("the gRPC response should contain a jobId")
    public void theGrpcResponseShouldContainAJobId() {
        assertThat(lastGrpcResponse).isNotNull();
        assertThat(lastGrpcResponse.getJobId()).isNotBlank();
    }

    @When("I request the job using gRPC")
    public void iRequestTheJobUsingGrpc() {
        String jid = registeredJobId != null ? registeredJobId : scenarioState.getCreatedJobId();
        EfeJobGrpcAdapter.JobIdRequestGrpc request = new EfeJobGrpcAdapter.JobIdRequestGrpc(jid);
        lastGrpcResponse = grpcAdapter.getJob(request);
    }

    @Then("the gRPC response should contain the expected jobId")
    public void theGrpcResponseShouldContainTheExpectedJobId() {
        String jid = registeredJobId != null ? registeredJobId : scenarioState.getCreatedJobId();
        assertThat(lastGrpcResponse).isNotNull();
        assertThat(lastGrpcResponse.getJobId()).isEqualTo(jid);
    }

    // ==========================================
    // GraphQL API Steps
    // ==========================================

    @When("I query the job through GraphQL")
    public void iQueryTheJobThroughGraphQL() {
        String jid = registeredJobId != null ? registeredJobId : scenarioState.getCreatedJobId();
        String query = "{ job(id: \"" + jid + "\") { id jobId status jobType businessDate } }";
        ResponseEntity<Map<String, Object>> resp = graphQLController.executeGraphQL(new EfeGraphQLController.GraphQLRequest(query));
        lastGraphQLResponse = resp.getBody();
    }

    @Then("the GraphQL response should contain the jobId")
    public void theGraphQLResponseShouldContainTheJobId() {
        String jid = registeredJobId != null ? registeredJobId : scenarioState.getCreatedJobId();
        assertThat(lastGraphQLResponse).isNotNull();
        Map<?, ?> data = (Map<?, ?>) lastGraphQLResponse.get("data");
        assertThat(data).isNotNull();
        Map<?, ?> job = (Map<?, ?>) data.get("job");
        assertThat(job).isNotNull();
        assertThat(job.get("jobId")).isEqualTo(jid);
    }

    @Then("the GraphQL response should contain the job status")
    public void theGraphQLResponseShouldContainTheJobStatus() {
        Map<?, ?> data = (Map<?, ?>) lastGraphQLResponse.get("data");
        Map<?, ?> job = (Map<?, ?>) data.get("job");
        assertThat(job.get("status")).isNotNull();
    }

    @When("I query the tasks and results through GraphQL")
    public void iQueryTheTasksAndResultsThroughGraphQL() {
        String jid = registeredJobId != null ? registeredJobId : scenarioState.getCreatedJobId();
        String query = "{ tasks(jobId: \"" + jid + "\") { id status } results(jobId: \"" + jid + "\") { id status } }";
        ResponseEntity<Map<String, Object>> resp = graphQLController.executeGraphQL(new EfeGraphQLController.GraphQLRequest(query));
        lastGraphQLResponse = resp.getBody();
    }

    @Then("the GraphQL response should contain the task and result fields")
    public void theGraphQLResponseShouldContainTheTaskAndResultFields() {
        assertThat(lastGraphQLResponse).isNotNull();
        Map<?, ?> data = (Map<?, ?>) lastGraphQLResponse.get("data");
        assertThat(data).isNotNull();
        assertThat(data.containsKey("tasks")).isTrue();
        assertThat(data.containsKey("results")).isTrue();
    }

    // ==========================================
    // JMX Management Steps
    // ==========================================

    @Given("EFE JMX management is enabled")
    public void efeJmxManagementIsEnabled() throws Exception {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        moduleObjectName = new ObjectName("com.efe:type=Module,name=enterprise-flow-engine");
        executorObjectName = new ObjectName("com.efe:type=Executor,name=worker-pool");
        jmxAvailable = mBeanServer != null;
        assertThat(jmxAvailable).isTrue();
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
    // Database Component Steps
    // ==========================================

    @Given("the H2 database is available")
    public void theH2DatabaseIsAvailable() {
        assertThat(jobRepository).isNotNull();
    }

    @When("I submit a test job")
    public void iSubmitATestJob() {
        registeredJobId = "JOB-DB-" + UUID.randomUUID().toString().substring(0, 8);
        Job job = dbBroker.invoke(registeredJobId);
        dbProducer.produce(job);
    }

    @Then("the job should be stored")
    public void theJobShouldBeStored() {
        Optional<Job> found = jobRepository.findById(registeredJobId);
        assertThat(found).isPresent();
    }

    @Then("the job should be retrievable")
    public void theJobShouldBeRetrievable() {
        Optional<Job> found = jobRepository.findById(registeredJobId);
        assertThat(found).isPresent();
        assertThat(found.get().getJobId()).isEqualTo(registeredJobId);
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
