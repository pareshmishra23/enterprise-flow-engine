package com.efe.traderecon.api.graphql;

import com.efe.traderecon.api.controller.JobController;
import com.efe.traderecon.api.dto.JobSubmissionRequest;
import com.efe.traderecon.api.dto.JobSubmissionResponse;
import com.efe.traderecon.domain.*;
import com.efe.traderecon.persistence.spi.JobRepository;
import com.efe.traderecon.persistence.spi.ResultRepository;
import com.efe.traderecon.persistence.spi.TaskRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EFE GraphQL Controller.
 * Provides a GraphQL query/mutation entry point into EFE:
 * - Query: job(id: "...")
 * - Query: tasks(jobId: "...")
 * - Query: results(jobId: "...")
 * - Mutation: submitJob(jobType: "...", businessDate: "...")
 *
 * Delegates to the same underlying EFE repositories and JobController.
 */
@RestController
@RequestMapping("/graphql")
public class EfeGraphQLController {

    private final JobRepository jobRepository;
    private final TaskRepository taskRepository;
    private final ResultRepository resultRepository;
    private final JobController jobController;

    private static final Pattern JOB_QUERY_PATTERN = Pattern.compile("job\\s*\\(\\s*id\\s*:\\s*\"([^\"]+)\"\\s*\\)");
    private static final Pattern TASKS_QUERY_PATTERN = Pattern.compile("tasks\\s*\\(\\s*jobId\\s*:\\s*\"([^\"]+)\"\\s*\\)");
    private static final Pattern RESULTS_QUERY_PATTERN = Pattern.compile("results\\s*\\(\\s*jobId\\s*:\\s*\"([^\"]+)\"\\s*\\)");
    private static final Pattern SUBMIT_MUTATION_PATTERN = Pattern.compile("submitJob\\s*\\(\\s*jobType\\s*:\\s*\"([^\"]+)\"\\s*,\\s*businessDate\\s*:\\s*\"([^\"]+)\"\\s*\\)");

    public EfeGraphQLController(JobRepository jobRepository,
                                TaskRepository taskRepository,
                                ResultRepository resultRepository,
                                JobController jobController) {
        this.jobRepository = jobRepository;
        this.taskRepository = taskRepository;
        this.resultRepository = resultRepository;
        this.jobController = jobController;
    }

    public static class GraphQLRequest {
        private String query;
        private Map<String, Object> variables;

        public GraphQLRequest() {}
        public GraphQLRequest(String query) { this.query = query; }

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }

        public Map<String, Object> getVariables() { return variables; }
        public void setVariables(Map<String, Object> variables) { this.variables = variables; }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> executeGraphQL(@RequestBody GraphQLRequest request) {
        String query = request.getQuery();
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> response = new HashMap<>();

        if (query == null || query.isBlank()) {
            response.put("errors", List.of(Map.of("message", "Empty query")));
            return ResponseEntity.badRequest().body(response);
        }

        // 1. Query: job(id: "...")
        Matcher jobMatcher = JOB_QUERY_PATTERN.matcher(query);
        if (jobMatcher.find()) {
            String jobId = jobMatcher.group(1);
            Optional<Job> jobOpt = jobRepository.findById(jobId);
            if (jobOpt.isPresent()) {
                Job job = jobOpt.get();
                Map<String, Object> jobMap = new HashMap<>();
                jobMap.put("id", job.getJobId());
                jobMap.put("jobId", job.getJobId());
                jobMap.put("jobType", job.getJobType() != null ? job.getJobType().name() : null);
                jobMap.put("status", job.getStatus() != null ? job.getStatus().name() : null);
                jobMap.put("businessDate", job.getBusinessDate() != null ? job.getBusinessDate().toString() : null);
                data.put("job", jobMap);
            } else {
                data.put("job", null);
            }
        }

        // 2. Query: tasks(jobId: "...")
        Matcher tasksMatcher = TASKS_QUERY_PATTERN.matcher(query);
        if (tasksMatcher.find()) {
            String jobId = tasksMatcher.group(1);
            List<Task> tasks = taskRepository.findByJobId(jobId);
            List<Map<String, Object>> taskList = new ArrayList<>();
            for (Task t : tasks) {
                Map<String, Object> tm = new HashMap<>();
                tm.put("id", t.getTaskId());
                tm.put("taskId", t.getTaskId());
                tm.put("jobId", t.getJobId());
                tm.put("status", t.getStatus() != null ? t.getStatus().name() : null);
                taskList.add(tm);
            }
            data.put("tasks", taskList);
        }

        // 3. Query: results(jobId: "...")
        Matcher resultsMatcher = RESULTS_QUERY_PATTERN.matcher(query);
        if (resultsMatcher.find()) {
            String jobId = resultsMatcher.group(1);
            List<ReconciliationResult> results = resultRepository.findByJobId(jobId);
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (ReconciliationResult r : results) {
                Map<String, Object> rm = new HashMap<>();
                rm.put("id", r.getResultId());
                rm.put("resultId", r.getResultId());
                rm.put("jobId", r.getJobId());
                rm.put("differenceType", r.getDifferenceType() != null ? r.getDifferenceType().name() : null);
                resultList.add(rm);
            }
            data.put("results", resultList);
        }

        // 4. Mutation: submitJob(...)
        Matcher submitMatcher = SUBMIT_MUTATION_PATTERN.matcher(query);
        if (submitMatcher.find()) {
            String jobTypeStr = submitMatcher.group(1);
            String businessDateStr = submitMatcher.group(2);

            JobSubmissionRequest subReq = new JobSubmissionRequest();
            subReq.setJobType(jobTypeStr);
            subReq.setBusinessDate(LocalDate.parse(businessDateStr));

            ResponseEntity<JobSubmissionResponse> subResp = jobController.submitJob(subReq, null, null, null);
            JobSubmissionResponse jr = subResp.getBody();
            if (jr != null) {
                Map<String, Object> jobMap = new HashMap<>();
                jobMap.put("id", jr.getJobId());
                jobMap.put("jobId", jr.getJobId());
                jobMap.put("jobType", jr.getJobType());
                jobMap.put("status", jr.getStatus());
                jobMap.put("createdAt", jr.getAcceptedAt() != null ? jr.getAcceptedAt().toString() : null);
                data.put("submitJob", jobMap);
            }
        }

        response.put("data", data);
        return ResponseEntity.ok(response);
    }
}
