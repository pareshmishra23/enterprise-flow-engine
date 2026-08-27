package com.efe.traderecon.cucumber.support;

import com.efe.traderecon.api.dto.JobSubmissionRequest;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

@Component
public class ScenarioState {

    private JobSubmissionRequest jobSubmissionRequest;
    private String idempotencyKey;
    private String correlationId;
    private final Map<String, String> requestHeaders = new HashMap<>();
    private MvcResult latestMvcResult;
    private int latestStatusCode;
    private String latestResponseBody;
    private String createdJobId;

    public void reset() {
        this.jobSubmissionRequest = null;
        this.idempotencyKey = null;
        this.correlationId = null;
        this.requestHeaders.clear();
        this.latestMvcResult = null;
        this.latestStatusCode = 0;
        this.latestResponseBody = null;
        this.createdJobId = null;
    }

    public JobSubmissionRequest getJobSubmissionRequest() {
        return jobSubmissionRequest;
    }

    public void setJobSubmissionRequest(JobSubmissionRequest jobSubmissionRequest) {
        this.jobSubmissionRequest = jobSubmissionRequest;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setHeader(String name, String value) {
        this.requestHeaders.put(name, value);
    }

    public MvcResult getLatestMvcResult() {
        return latestMvcResult;
    }

    public void setLatestMvcResult(MvcResult latestMvcResult) {
        this.latestMvcResult = latestMvcResult;
        if (latestMvcResult != null) {
            this.latestStatusCode = latestMvcResult.getResponse().getStatus();
            try {
                this.latestResponseBody = latestMvcResult.getResponse().getContentAsString();
            } catch (Exception e) {
                this.latestResponseBody = "";
            }
        }
    }

    public int getLatestStatusCode() {
        return latestStatusCode;
    }

    public String getLatestResponseBody() {
        return latestResponseBody;
    }

    public String getCreatedJobId() {
        return createdJobId;
    }

    public void setCreatedJobId(String createdJobId) {
        this.createdJobId = createdJobId;
    }
}
