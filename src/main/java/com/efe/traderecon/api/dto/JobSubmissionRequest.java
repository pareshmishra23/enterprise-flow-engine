package com.efe.traderecon.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.Map;

public class JobSubmissionRequest {

    private String jobType;
    private String source;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate businessDate;

    private Map<String, Object> payload;

    public JobSubmissionRequest() {
    }

    public JobSubmissionRequest(String jobType, String source, LocalDate businessDate, Map<String, Object> payload) {
        this.jobType = jobType;
        this.source = source;
        this.businessDate = businessDate;
        this.payload = payload;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
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

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "JobSubmissionRequest{" +
                "jobType='" + jobType + '\'' +
                ", source='" + source + '\'' +
                ", businessDate=" + businessDate +
                '}';
    }
}
