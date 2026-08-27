package com.efe.traderecon.cucumber.steps;

import com.efe.traderecon.api.controller.JobController;
import com.efe.traderecon.cucumber.support.ScenarioState;
import com.efe.traderecon.domain.DifferenceType;
import com.efe.traderecon.domain.Job;
import com.efe.traderecon.domain.JobStatus;
import com.efe.traderecon.domain.JobType;
import com.efe.traderecon.domain.ReconciliationResult;
import com.efe.traderecon.persistence.spi.JobRepository;
import com.efe.traderecon.persistence.spi.ResultRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class ResultQuerySteps {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ResultRepository resultRepository;

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

    @Given("a completed job with results")
    public void aCompletedJobWithResults() {
        String jobId = "JOB-RES-01";
        Job job = new Job(jobId, JobType.RECONCILIATION, "CUSTODIAN", LocalDate.now(), 2);
        job.setStatus(JobStatus.COMPLETED);
        jobRepository.save(job);

        ReconciliationResult r1 = new ReconciliationResult("RES-01", jobId, "TSK-01", "TR-01", DifferenceType.MATCH, BigDecimal.ZERO);
        ReconciliationResult r2 = new ReconciliationResult("RES-02", jobId, "TSK-01", "TR-02", DifferenceType.QUANTITY_BREAK, new BigDecimal("10"));
        resultRepository.saveAll(List.of(r1, r2));

        scenarioState.setCreatedJobId(jobId);
    }

    @When("I request the results for that job")
    public void iRequestTheResultsForThatJob() throws Exception {
        MvcResult result = getMockMvc().perform(get("/api/v1/jobs/" + scenarioState.getCreatedJobId() + "/results")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        scenarioState.setLatestMvcResult(result);
    }

    @And("the response should contain a result collection")
    public void theResponseShouldContainAResultCollection() throws Exception {
        JsonNode json = objectMapper.readTree(scenarioState.getLatestResponseBody());
        assertThat(json.has("results")).isTrue();
        assertThat(json.get("results").isArray()).isTrue();
        assertThat(json.get("results").size()).isGreaterThan(0);
    }

    @And("every returned result should belong to the requested job")
    public void everyReturnedResultShouldBelongToTheRequestedJob() throws Exception {
        JsonNode json = objectMapper.readTree(scenarioState.getLatestResponseBody());
        assertThat(json.has("jobId")).isTrue();
        assertThat(json.get("jobId").asText()).isEqualTo(scenarioState.getCreatedJobId());
    }
}
