package com.efe.traderecon.cucumber.steps;

import com.efe.traderecon.api.controller.JobController;
import com.efe.traderecon.api.dto.JobSubmissionRequest;
import com.efe.traderecon.api.graphql.EfeGraphQLController;
import com.efe.traderecon.api.grpc.EfeJobGrpcAdapter;
import com.efe.traderecon.cucumber.support.ScenarioState;
import com.efe.traderecon.domain.Job;
import com.efe.traderecon.flow.dbdemo.DbDemoFlowConfiguration;
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

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Reconciliation-example acceptance steps for the reconciliation API surface (REST, gRPC, GraphQL)
 * and the DB capability. These exercise the reference application's own domain and services.
 */
public class ReconciliationApiSteps {

    @Autowired
    private JobController jobController;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private EfeJobGrpcAdapter grpcAdapter;

    @Autowired
    private EfeGraphQLController graphQLController;

    @Autowired
    private DbDemoFlowConfiguration.DbScheduledConsumer dbConsumer;

    @Autowired
    private DbDemoFlowConfiguration.DatabaseAccessBroker dbBroker;

    @Autowired
    private DbDemoFlowConfiguration.DbResultProducer dbProducer;

    @Autowired
    private ScenarioState scenarioState;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private EfeJobGrpcAdapter.JobResponseGrpc lastGrpcResponse;
    private Map<String, Object> lastGraphQLResponse;
    private String registeredJobId;

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
    // Database Component Steps
    // ==========================================

    @Given("the H2 database is available")
    public void theH2DatabaseIsAvailable() {
        assertThat(jobRepository).isNotNull();
    }

    @When("I submit a test job")
    public void iSubmitATestJob() {
        registeredJobId = "JOB-DB-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        Job job = dbBroker.invoke(registeredJobId);
        dbProducer.produce(job);
    }

    @Then("the job should be stored")
    public void theJobShouldBeStored() {
        Optional<Job> found = jobRepository.findById(registeredJobId);
        assertThat(found).isPresent();
    }

    @And("the job should be retrievable")
    public void theJobShouldBeRetrievable() {
        Optional<Job> found = jobRepository.findById(registeredJobId);
        assertThat(found).isPresent();
        assertThat(found.get().getJobId()).isEqualTo(registeredJobId);
    }
}
