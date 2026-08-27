package com.efe.traderecon.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public class Job {
    private String jobId;
    private JobType jobType;
    private String source;
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

    public Job() {
        this.createdAt = Instant.now();
        this.status = JobStatus.SUBMITTED;
        this.jobType = JobType.RECONCILIATION;
    }

    public Job(String jobId, JobType jobType, String source, LocalDate businessDate, int totalRecords) {
        this.jobId = jobId;
        this.jobType = jobType != null ? jobType : JobType.RECONCILIATION;
        this.source = source;
        this.businessDate = businessDate;
        this.totalRecords = totalRecords;
        this.status = JobStatus.SUBMITTED;
        this.createdAt = Instant.now();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return Objects.equals(jobId, job.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId);
    }

    @Override
    public String toString() {
        return "Job{" +
                "jobId='" + jobId + '\'' +
                ", jobType=" + jobType +
                ", source='" + source + '\'' +
                ", businessDate=" + businessDate +
                ", status=" + status +
                ", totalRecords=" + totalRecords +
                ", processedRecords=" + processedRecords +
                ", matchedRecords=" + matchedRecords +
                ", breakRecords=" + breakRecords +
                ", failedRecords=" + failedRecords +
                ", createdAt=" + createdAt +
                ", startedAt=" + startedAt +
                ", completedAt=" + completedAt +
                '}';
    }
}
