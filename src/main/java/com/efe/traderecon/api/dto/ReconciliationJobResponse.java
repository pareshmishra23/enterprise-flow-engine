package com.efe.traderecon.api.dto;

import com.efe.traderecon.domain.JobStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.time.LocalDate;

public class ReconciliationJobResponse {
    private String jobId;
    private JobStatus status;
    private String source;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate businessDate;
    private int totalRecords;
    private String message;
    private Instant createdAt;

    public ReconciliationJobResponse() {
    }

    public ReconciliationJobResponse(String jobId, JobStatus status, String source, LocalDate businessDate, int totalRecords, String message, Instant createdAt) {
        this.jobId = jobId;
        this.status = status;
        this.source = source;
        this.businessDate = businessDate;
        this.totalRecords = totalRecords;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDate getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(LocalDate businessDate) {
        this.businessDate = businessDate;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
