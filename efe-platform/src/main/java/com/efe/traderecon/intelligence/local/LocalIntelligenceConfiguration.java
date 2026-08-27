package com.efe.traderecon.intelligence.local;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * EFE Local Intelligence Configuration.
 * Ensures ObjectMapper is always available for parsing Ollama responses.
 */
@Configuration
public class LocalIntelligenceConfiguration {

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
