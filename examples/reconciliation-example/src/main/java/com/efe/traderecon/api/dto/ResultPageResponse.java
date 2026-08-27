package com.efe.traderecon.api.dto;

import java.util.ArrayList;
import java.util.List;

public class ResultPageResponse {
    private String jobId;
    private int page;
    private int size;
    private long totalElements;
    private List<Object> results = new ArrayList<>();

    public ResultPageResponse() {
    }

    public ResultPageResponse(String jobId, int page, int size, long totalElements, List<Object> results) {
        this.jobId = jobId;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.results = results != null ? results : new ArrayList<>();
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

    public List<Object> getResults() {
        return results;
    }

    public void setResults(List<Object> results) {
        this.results = results;
    }
}
