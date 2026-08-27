package com.efe.traderecon.intelligence.local;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * EFE Ollama HTTP Client.
 *
 * This is the ONLY class in EFE that contains Ollama-specific logic.
 * It must NEVER be referenced by business processors, REST controllers,
 * or Ikasan flow components — only by local IntelligenceProvider implementations.
 *
 * Future providers (OpenAI, Azure OpenAI, etc.) replace this class entirely.
 */
@Component
public class OllamaClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaClient.class);

    private final LocalIntelligenceProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OllamaClient(LocalIntelligenceProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getTimeoutMs()))
                .build();
    }

    /**
     * Send a prompt to the Ollama /api/generate endpoint.
     * Returns the raw model response string, or throws on network/timeout failure.
     */
    public String generate(String modelName, String prompt) throws Exception {
        String endpoint = properties.getLocal().getEndpoint() + "/api/generate";
        log.debug("Calling Ollama endpoint [{}] with model [{}]", endpoint, modelName);

        Map<String, Object> requestBody = Map.of(
                "model", modelName,
                "prompt", prompt,
                "stream", false,
                "format", "json"
        );

        String requestJson = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofMillis(properties.getTimeoutMs()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Ollama returned HTTP " + response.statusCode() + ": " + response.body());
        }

        // Ollama wraps the actual model output in a "response" field
        Map<?, ?> responseMap = objectMapper.readValue(response.body(), Map.class);
        Object modelResponse = responseMap.get("response");
        if (modelResponse == null) {
            throw new RuntimeException("Ollama response missing 'response' field: " + response.body());
        }

        log.debug("Ollama response received, length={}", modelResponse.toString().length());
        return modelResponse.toString();
    }
}
