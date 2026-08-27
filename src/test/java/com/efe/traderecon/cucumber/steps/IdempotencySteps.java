package com.efe.traderecon.cucumber.steps;

import com.efe.traderecon.api.controller.JobController;
import com.efe.traderecon.cucumber.support.ScenarioState;
import com.efe.traderecon.persistence.spi.JobRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class IdempotencySteps {

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

    private String originalJobId;

    private MockMvc mockMvc;

    private MockMvc getMockMvc() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.standaloneSetup(jobController)
                    .setControllerAdvice(globalExceptionHandler)
                    .build();
        }
        return mockMvc;
    }

    @And("the idempotency key is {string}")
    public void theIdempotencyKeyIs(String key) {
        scenarioState.setIdempotencyKey(key);
    }

    @When("I submit the same request again with idempotency key {string}")
    public void iSubmitTheSameRequestAgainWithIdempotencyKey(String key) throws Exception {
        this.originalJobId = scenarioState.getCreatedJobId();
        scenarioState.setIdempotencyKey(key);

        MvcResult result = getMockMvc().perform(post("/api/v1/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", key)
                        .content(objectMapper.writeValueAsString(scenarioState.getJobSubmissionRequest())))
                .andReturn();

        scenarioState.setLatestMvcResult(result);
    }

    @Then("the response should refer to the same jobId")
    public void theResponseShouldReferToTheSameJobId() throws Exception {
        JsonNode json = objectMapper.readTree(scenarioState.getLatestResponseBody());
        assertThat(json.has("jobId")).isTrue();
        assertThat(json.get("jobId").asText()).isEqualTo(this.originalJobId);
    }

    @And("only one job should exist for idempotency key {string}")
    public void onlyOneJobShouldExistForIdempotencyKey(String key) {
        assertThat(jobRepository.findAll()).hasSize(1);
    }
}
