package com.efe.traderecon.cucumber.steps;

import com.efe.traderecon.api.controller.HealthController;
import com.efe.traderecon.api.controller.JobController;
import com.efe.traderecon.cucumber.support.ScenarioState;
import com.efe.traderecon.domain.Job;
import com.efe.traderecon.domain.JobStatus;
import com.efe.traderecon.domain.JobType;
import com.efe.traderecon.persistence.spi.JobRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class JobStatusSteps {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobController jobController;

    @Autowired
    private HealthController healthController;

    @Autowired
    private com.efe.traderecon.api.controller.GlobalExceptionHandler globalExceptionHandler;

    @Autowired
    private ScenarioState scenarioState;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc jobMockMvc;
    private MockMvc healthMockMvc;

    private MockMvc getJobMockMvc() {
        if (jobMockMvc == null) {
            jobMockMvc = MockMvcBuilders.standaloneSetup(jobController)
                    .setControllerAdvice(globalExceptionHandler)
                    .build();
        }
        return jobMockMvc;
    }

    private MockMvc getHealthMockMvc() {
        if (healthMockMvc == null) {
            healthMockMvc = MockMvcBuilders.standaloneSetup(healthController).build();
        }
        return healthMockMvc;
    }

    @Given("a job has been registered")
    public void aJobHasBeenRegistered() {
        String jobId = "JOB-STATUS-01";
        Job job = new Job(jobId, JobType.RECONCILIATION, "CUSTODIAN", LocalDate.now(), 5);
        job.setStatus(JobStatus.SUBMITTED);
        job.setCreatedAt(Instant.now());
        jobRepository.save(job);
        scenarioState.setCreatedJobId(jobId);
    }

    @Given("jobId {string} does not exist")
    public void jobIdDoesNotExist(String nonExistentId) {
        jobRepository.deleteById(nonExistentId);
    }

    @When("I request {string}")
    public void iRequest(String path) throws Exception {
        String resolvedPath = path;
        if (path.contains("{jobId}")) {
            resolvedPath = path.replace("{jobId}", scenarioState.getCreatedJobId());
        }

        MvcResult result;
        if (resolvedPath.equals("/health") || resolvedPath.equals("/ready")) {
            result = getHealthMockMvc().perform(get(resolvedPath)
                            .accept(MediaType.APPLICATION_JSON))
                    .andReturn();
        } else {
            result = getJobMockMvc().perform(get(resolvedPath)
                            .accept(MediaType.APPLICATION_JSON))
                    .andReturn();
        }
        scenarioState.setLatestMvcResult(result);
    }

    @And("the response should contain the jobId")
    public void theResponseShouldContainTheJobId() throws Exception {
        JsonNode json = objectMapper.readTree(scenarioState.getLatestResponseBody());
        assertThat(json.has("jobId")).isTrue();
        assertThat(json.get("jobId").asText()).isEqualTo(scenarioState.getCreatedJobId());
    }

    @And("the response should contain the jobType")
    public void theResponseShouldContainTheJobType() throws Exception {
        JsonNode json = objectMapper.readTree(scenarioState.getLatestResponseBody());
        assertThat(json.has("jobType")).isTrue();
    }

    @And("the response should contain the job status")
    public void theResponseShouldContainTheJobStatus() throws Exception {
        JsonNode json = objectMapper.readTree(scenarioState.getLatestResponseBody());
        assertThat(json.has("status")).isTrue();
    }
}
