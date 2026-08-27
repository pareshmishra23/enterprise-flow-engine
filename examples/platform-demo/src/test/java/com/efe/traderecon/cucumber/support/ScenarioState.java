package com.efe.traderecon.cucumber.support;

import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

@Component
public class ScenarioState {

    private final Map<String, String> requestHeaders = new HashMap<>();
    private MvcResult latestMvcResult;
    private int latestStatusCode;
    private String latestResponseBody;

    public void reset() {
        this.requestHeaders.clear();
        this.latestMvcResult = null;
        this.latestStatusCode = 0;
        this.latestResponseBody = null;
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
}
