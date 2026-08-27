package com.efe.traderecon.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TaskResult {
    private String taskId;
    private String jobId;
    private TaskStatus status;
    private int matchedCount;
    private int breakCount;
    private int failureCount;
    private String message;
    private List<ReconciliationResult> results;
    private Instant completedAt;

    public TaskResult() {
        this.results = new ArrayList<>();
        this.completedAt = Instant.now();
    }

    public TaskResult(String taskId, String jobId, TaskStatus status) {
        this();
        this.taskId = taskId;
        this.jobId = jobId;
        this.status = status;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public int getMatchedCount() {
        return matchedCount;
    }

    public void setMatchedCount(int matchedCount) {
        this.matchedCount = matchedCount;
    }

    public int getBreakCount() {
        return breakCount;
    }

    public void setBreakCount(int breakCount) {
        this.breakCount = breakCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ReconciliationResult> getResults() {
        return results;
    }

    public void setResults(List<ReconciliationResult> results) {
        this.results = results;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public String toString() {
        return "TaskResult{" +
                "taskId='" + taskId + '\'' +
                ", jobId='" + jobId + '\'' +
                ", status=" + status +
                ", matchedCount=" + matchedCount +
                ", breakCount=" + breakCount +
                ", failureCount=" + failureCount +
                ", message='" + message + '\'' +
                ", completedAt=" + completedAt +
                '}';
    }
}
