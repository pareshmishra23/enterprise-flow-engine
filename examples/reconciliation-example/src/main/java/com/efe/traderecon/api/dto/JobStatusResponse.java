package com.efe.traderecon.api.dto;

import com.efe.traderecon.domain.Job;
import com.efe.traderecon.domain.JobStatus;
import com.efe.traderecon.domain.JobType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.time.LocalDate;

public class JobStatusResponse {
    private String jobId;
    private JobType jobType;
    private String source;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate businessDate;
    private JobStatus status;
    private int totalRecords;
    private int processedRecords;
    private int matchedRecords;
    private int breakRecords;
    private int failedRecords;
    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;

    public JobStatusResponse() {
    }

    public static JobStatusResponse fromDomain(Job job) {
        if (job == null) return null;
        JobStatusResponse resp = new JobStatusResponse();
        resp.setJobId(job.getJobId());
        resp.setJobType(job.getJobType());
        resp.setSource(job.getSource());
        resp.setBusinessDate(job.getBusinessDate());
        resp.setStatus(job.getStatus());
        resp.setTotalRecords(job.getTotalRecords());
        resp.setProcessedRecords(job.getProcessedRecords());
        resp.setMatchedRecords(job.getMatchedRecords());
        resp.setBreakRecords(job.getBreakRecords());
        resp.setFailedRecords(job.getFailedRecords());
        resp.setCreatedAt(job.getCreatedAt());
        resp.setStartedAt(job.getStartedAt());
        resp.setCompletedAt(job.getCompletedAt());
        return resp;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
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

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getProcessedRecords() {
        return processedRecords;
    }

    public void setProcessedRecords(int processedRecords) {
        this.processedRecords = processedRecords;
    }

    public int getMatchedRecords() {
        return matchedRecords;
    }

    public void setMatchedRecords(int matchedRecords) {
        this.matchedRecords = matchedRecords;
    }

    public int getBreakRecords() {
        return breakRecords;
    }

    public void setBreakRecords(int breakRecords) {
        this.breakRecords = breakRecords;
    }

    public int getFailedRecords() {
        return failedRecords;
    }

    public void setFailedRecords(int failedRecords) {
        this.failedRecords = failedRecords;
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
}
