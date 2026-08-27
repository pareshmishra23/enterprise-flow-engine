package com.efe.traderecon.cucumber.steps;

import com.efe.traderecon.api.controller.JobController;
import com.efe.traderecon.cucumber.support.ScenarioState;
import com.efe.traderecon.domain.Job;
import com.efe.traderecon.domain.JobStatus;
import com.efe.traderecon.domain.JobType;
import com.efe.traderecon.domain.Task;
import com.efe.traderecon.persistence.spi.JobRepository;
import com.efe.traderecon.persistence.spi.TaskRepository;
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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class TaskQuerySteps {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private TaskRepository taskRepository;

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

    @Given("a registered job with tasks")
    public void aRegisteredJobWithTasks() {
        String jobId = "JOB-TASK-01";
        Job job = new Job(jobId, JobType.RECONCILIATION, "CUSTODIAN", LocalDate.now(), 2);
        job.setStatus(JobStatus.SUBMITTED);
        jobRepository.save(job);

        Task t1 = new Task("TSK-Q-01", jobId, "TRADE_RECONCILIATION");
        Task t2 = new Task("TSK-Q-02", jobId, "TRADE_RECONCILIATION");
        taskRepository.saveAll(List.of(t1, t2));

        scenarioState.setCreatedJobId(jobId);
    }

    @When("I request the tasks for that job")
    public void iRequestTheTasksForThatJob() throws Exception {
        MvcResult result = getMockMvc().perform(get("/api/v1/jobs/" + scenarioState.getCreatedJobId() + "/tasks")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        scenarioState.setLatestMvcResult(result);
    }

    @And("the response should contain a task collection")
    public void theResponseShouldContainATaskCollection() throws Exception {
        JsonNode json = objectMapper.readTree(scenarioState.getLatestResponseBody());
        assertThat(json.has("tasks")).isTrue();
        assertThat(json.get("tasks").isArray()).isTrue();
        assertThat(json.get("tasks").size()).isGreaterThan(0);
    }

    @And("every returned task should belong to the requested job")
    public void everyReturnedTaskShouldBelongToTheRequestedJob() throws Exception {
        JsonNode json = objectMapper.readTree(scenarioState.getLatestResponseBody());
        assertThat(json.has("jobId")).isTrue();
        assertThat(json.get("jobId").asText()).isEqualTo(scenarioState.getCreatedJobId());
    }
}
