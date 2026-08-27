package com.efe.traderecon.intelligence.sanitizer;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "efe.intelligence.sanitizer")
public class SanitizerProperties {

    private List<String> maskedFields = List.of("accountId", "customerId", "nationalId", "taxId");

    public List<String> getMaskedFields() { return maskedFields; }
    public void setMaskedFields(List<String> maskedFields) { this.maskedFields = maskedFields; }
}
