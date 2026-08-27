package com.efe.traderecon.cucumber.steps;

import com.efe.traderecon.api.controller.JobController;
import com.efe.traderecon.api.dto.JobSubmissionRequest;
import com.efe.traderecon.cucumber.support.ScenarioState;
import com.efe.traderecon.ikasan.engine.IkasanEngine;
import com.efe.traderecon.messaging.spi.MessagingBrokerFactory;
import com.efe.traderecon.persistence.spi.JobRepository;
import com.efe.traderecon.persistence.spi.PersistenceProviderFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class JobSubmissionSteps {

    @Autowired
    private IkasanEngine ikasanEngine;

    @Autowired
    private MessagingBrokerFactory messagingBrokerFactory;

    @Autowired
    private PersistenceProviderFactory persistenceProviderFactory;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobController jobController;

    @Autowired
    private com.efe.traderecon.api.controller.GlobalExceptionHandler globalExceptionHandler;

    @Autowired
    private ScenarioState scenarioState;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private MockMvc getMockMvc() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.standaloneSetup(jobController)
                    .setControllerAdvice(globalExceptionHandler)
                    .build();
        }
        return mockMvc;
    }

    @Given("EFE is running")
    public void efeIsRunning() {
        assertThat(ikasanEngine.getModule().isRunning()).isTrue();
    }

    @And("the messaging provider is {string}")
    public void theMessagingProviderIs(String providerName) {
        assertThat(messagingBrokerFactory.getProvider(providerName).isAvailable()).isTrue();
    }

    @And("the persistence provider is {string}")
    public void thePersistenceProviderIs(String providerName) {
        assertThat(persistenceProviderFactory.getProvider(providerName).isAvailable()).isTrue();
    }

    @Given("I have a valid job request with jobType {string}")
    public void iHaveAValidJobRequestWithJobType(String jobType) {
        JobSubmissionRequest req = new JobSubmissionRequest(
                jobType,
                "CUSTODIAN",
                LocalDate.of(2026, 8, 27),
                Map.of("records", List.of(
                        Map.of("tradeId", "TR-101", "accountId", "ACC-1", "securityId", "SEC-A", "quantity", 100, "price", 50)
                ))
        );
        scenarioState.setJobSubmissionRequest(req);
    }

    @Given("I have a valid job request")
    public void iHaveAValidJobRequest() {
        iHaveAValidJobRequestWithJobType("TRADE_RECONCILIATION");
    }

    @Given("I have a job request without a jobType")
    public void iHaveAJobRequestWithoutAJobType() {
        JobSubmissionRequest req = new JobSubmissionRequest(
                null,
                "CUSTODIAN",
                LocalDate.of(2026, 8, 27),
                Map.of("records", List.of())
        );
        scenarioState.setJobSubmissionRequest(req);
    }

    @Given("I have a job request without a businessDate")
    public void iHaveAJobRequestWithoutABusinessDate() {
        JobSubmissionRequest req = new JobSubmissionRequest(
                "TRADE_RECONCILIATION",
                "CUSTODIAN",
                null,
                Map.of("records", List.of())
        );
        scenarioState.setJobSubmissionRequest(req);
    }

    @When("I submit the request to {string}")
    public void iSubmitTheRequestTo(String endpoint) throws Exception {
        MockHttpServletRequestBuilder builder = post(endpoint)
                .contentType(MediaType.APPLICATION_JSON);

        if (scenarioState.getJobSubmissionRequest() != null) {
            builder.content(objectMapper.writeValueAsString(scenarioState.getJobSubmissionRequest()));
        }

        if (scenarioState.getIdempotencyKey() != null) {
            builder.header("Idempotency-Key", scenarioState.getIdempotencyKey());
        }

        if (scenarioState.getCorrelationId() != null) {
            builder.header("X-Correlation-ID", scenarioState.getCorrelationId());
        }

        MvcResult result = getMockMvc().perform(builder).andReturn();
        scenarioState.setLatestMvcResult(result);

        if (result.getResponse().getStatus() == 201) {
            JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
            if (json.has("jobId")) {
                scenarioState.setCreatedJobId(json.get("jobId").asText());
            }
        }
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertThat(scenarioState.getLatestStatusCode()).isEqualTo(expectedStatus);
    }

    @And("the response should contain a jobId")
    public void theResponseShouldContainAJobId() throws Exception {
        JsonNode json = objectMapper.readTree(scenarioState.getLatestResponseBody());
        assertThat(json.has("jobId")).isTrue();
        assertThat(json.get("jobId").asText()).isNotBlank().startsWith("JOB-");
    }

    @And("the response status should be {string}")
    public void theResponseStatusShouldBe(String expectedStatus) throws Exception {
        JsonNode json = objectMapper.readTree(scenarioState.getLatestResponseBody());
        assertThat(json.has("status")).isTrue();
        assertThat(json.get("status").asText()).isEqualTo(expectedStatus);
    }

    @And("the response should contain a Location header")
    public void theResponseShouldContainALocationHeader() {
        String location = scenarioState.getLatestMvcResult().getResponse().getHeader("Location");
        assertThat(location).isNotNull().startsWith("/api/v1/jobs/JOB-");
    }

    @And("the job should exist in EFE")
    public void theJobShouldExistInEFE() {
        assertThat(scenarioState.getCreatedJobId()).isNotNull();
        assertThat(jobRepository.findById(scenarioState.getCreatedJobId())).isPresent();
    }

    @And("the response errorCode should be {string}")
    public void theResponseErrorCodeShouldBe(String expectedErrorCode) throws Exception {
        JsonNode json = objectMapper.readTree(scenarioState.getLatestResponseBody());
        assertThat(json.has("errorCode")).isTrue();
        assertThat(json.get("errorCode").asText()).isEqualTo(expectedErrorCode);
    }

    @And("no job should be created")
    public void noJobShouldBeCreated() {
        assertThat(jobRepository.findAll()).isEmpty();
    }
}
