package com.efe.traderecon.intelligence.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * EFE Intelligence Registry.
 * Spring-managed collection of all registered IntelligenceProvider implementations.
 * Providers register automatically by being Spring @Component / @Service beans.
 */
@Component
public class IntelligenceRegistry {

    private static final Logger log = LoggerFactory.getLogger(IntelligenceRegistry.class);

    private final List<IntelligenceProvider> providers;

    public IntelligenceRegistry(List<IntelligenceProvider> providers) {
        this.providers = providers;
        log.info("IntelligenceRegistry initialized with {} provider(s): {}",
                providers.size(),
                providers.stream().map(IntelligenceProvider::getProviderName).toList());
    }

    public IntelligenceProvider getProvider(IntelligenceType type) {
        return providers.stream()
                .filter(p -> p.supports(type))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No IntelligenceProvider found for type: " + type));
    }

    public List<IntelligenceProvider> getAllProviders() {
        return providers;
    }
}
