package com.efe.traderecon.persistence.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PersistenceProviderFactory {
    private static final Logger log = LoggerFactory.getLogger(PersistenceProviderFactory.class);

    private final Map<String, PersistenceProvider> providers;

    public PersistenceProviderFactory(List<PersistenceProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(p -> p.getName().toLowerCase(), p -> p));
        log.info("Initialized PersistenceProviderFactory with registered providers: {}", providers.keySet());
    }

    public PersistenceProvider getProvider(String providerName) {
        if (providerName == null || providerName.isBlank()) {
            throw new IllegalArgumentException("Persistence provider name must not be empty");
        }
        PersistenceProvider provider = providers.get(providerName.trim().toLowerCase());
        if (provider == null) {
            throw new IllegalArgumentException(
                    "Unknown persistence provider: '" + providerName + "'. Available providers: " + providers.keySet()
            );
        }
        if (!provider.isAvailable()) {
            throw new IllegalStateException("Persistence provider '" + providerName + "' is not available or not enabled in this bead");
        }
        return provider;
    }

    public List<String> getAvailableProviders() {
        return providers.entrySet().stream()
                .filter(e -> e.getValue().isAvailable())
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
    }
}
