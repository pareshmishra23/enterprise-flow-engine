package com.efe.traderecon.api.dto;

import java.util.ArrayList;
import java.util.List;

public class TaskPageResponse {
    private String jobId;
    private int page;
    private int size;
    private long totalElements;
    private List<TaskSummaryResponse> tasks = new ArrayList<>();

    public TaskPageResponse() {
    }

    public TaskPageResponse(String jobId, int page, int size, long totalElements, List<TaskSummaryResponse> tasks) {
        this.jobId = jobId;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.tasks = tasks != null ? tasks : new ArrayList<>();
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public List<TaskSummaryResponse> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskSummaryResponse> tasks) {
        this.tasks = tasks;
    }
}
