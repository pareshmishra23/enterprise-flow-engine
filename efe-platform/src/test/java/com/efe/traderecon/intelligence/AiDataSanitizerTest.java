package com.efe.traderecon.intelligence;

import com.efe.traderecon.intelligence.sanitizer.AiDataSanitizer;
import com.efe.traderecon.intelligence.sanitizer.SanitizerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AiDataSanitizer — Sensitive field masking unit tests")
class AiDataSanitizerTest {

    private AiDataSanitizer sanitizer;

    @BeforeEach
    void setUp() {
        SanitizerProperties properties = new SanitizerProperties();
        properties.setMaskedFields(List.of("accountId", "customerId", "nationalId", "taxId", "iban"));
        sanitizer = new AiDataSanitizer(properties);
    }

    @Test
    @DisplayName("Sensitive field is masked in sanitized copy")
    void sensitivFieldIsMasked() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tradeId", "T-001");
        payload.put("accountId", "ACC-12345");

        Map<String, Object> sanitized = sanitizer.sanitize(payload);

        assertThat(sanitized.get("accountId")).isEqualTo("***MASKED***");
        assertThat(sanitized.get("tradeId")).isEqualTo("T-001");
    }

    @Test
    @DisplayName("Original payload is NOT modified")
    void originalPayloadNotModified() {
        Map<String, Object> original = new HashMap<>();
        original.put("accountId", "ACC-999");
        original.put("quantity", "1000");

        sanitizer.sanitize(original);

        assertThat(original.get("accountId")).isEqualTo("ACC-999"); // unchanged
    }

    @Test
    @DisplayName("Multiple sensitive fields masked in one pass")
    void multipleFieldsMasked() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("accountId", "ACC-1");
        payload.put("customerId", "CUST-2");
        payload.put("iban", "GB29NWBK60161331926819");
        payload.put("tradeId", "T-001");

        Map<String, Object> sanitized = sanitizer.sanitize(payload);

        assertThat(sanitized.get("accountId")).isEqualTo("***MASKED***");
        assertThat(sanitized.get("customerId")).isEqualTo("***MASKED***");
        assertThat(sanitized.get("iban")).isEqualTo("***MASKED***");
        assertThat(sanitized.get("tradeId")).isEqualTo("T-001");
    }

    @Test
    @DisplayName("Null payload returns empty map without exception")
    void nullPayloadHandledGracefully() {
        Map<String, Object> sanitized = sanitizer.sanitize(null);

        assertThat(sanitized).isNotNull();
        assertThat(sanitized).isEmpty();
    }

    @Test
    @DisplayName("Payload with no sensitive fields passes through unchanged")
    void noSensitiveFieldsPassThrough() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tradeId", "T-002");
        payload.put("quantity", "500");

        Map<String, Object> sanitized = sanitizer.sanitize(payload);

        assertThat(sanitized).isEqualTo(payload);
    }
}
