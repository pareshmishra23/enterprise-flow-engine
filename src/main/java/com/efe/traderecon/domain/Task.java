package com.efe.traderecon.domain;

import java.time.Instant;
import java.util.Objects;

public class Task {
    private String taskId;
    private String jobId;
    private String taskType;
    private TaskStatus status;
    private int attemptCount;
    private int maxAttempts;
    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;

    public Task() {
        this.status = TaskStatus.PENDING;
        this.attemptCount = 0;
        this.maxAttempts = 3;
        this.createdAt = Instant.now();
        this.taskType = "TRADE_RECONCILIATION";
    }

    public Task(String taskId, String jobId, String taskType) {
        this();
        this.taskId = taskId;
        this.jobId = jobId;
        if (taskType != null) {
            this.taskType = taskType;
        }
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

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(taskId, task.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId);
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskId='" + taskId + '\'' +
                ", jobId='" + jobId + '\'' +
                ", taskType='" + taskType + '\'' +
                ", status=" + status +
                ", attemptCount=" + attemptCount +
                ", maxAttempts=" + maxAttempts +
                ", createdAt=" + createdAt +
                ", startedAt=" + startedAt +
                ", completedAt=" + completedAt +
                '}';
    }
}
