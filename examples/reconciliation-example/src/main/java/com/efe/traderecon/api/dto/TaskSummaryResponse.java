package com.efe.traderecon.api.dto;

public class TaskSummaryResponse {
    private String taskId;
    private String status;
    private int attemptCount;

    public TaskSummaryResponse() {
    }

    public TaskSummaryResponse(String taskId, String status, int attemptCount) {
        this.taskId = taskId;
        this.status = status;
        this.attemptCount = attemptCount;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }
}
