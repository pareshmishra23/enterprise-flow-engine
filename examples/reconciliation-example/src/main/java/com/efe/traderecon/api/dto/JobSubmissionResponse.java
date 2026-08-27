package com.efe.traderecon.api.dto;

import java.time.Instant;

public class JobSubmissionResponse {
    private String jobId;
    private String status;
    private String jobType;
    private Instant acceptedAt;

    public JobSubmissionResponse() {
    }

    public JobSubmissionResponse(String jobId, String status, String jobType, Instant acceptedAt) {
        this.jobId = jobId;
        this.status = status;
        this.jobType = jobType;
        this.acceptedAt = acceptedAt;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }
}
