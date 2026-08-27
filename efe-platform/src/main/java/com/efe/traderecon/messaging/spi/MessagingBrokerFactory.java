package com.efe.traderecon.messaging.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MessagingBrokerFactory {
    private static final Logger log = LoggerFactory.getLogger(MessagingBrokerFactory.class);

    private final Map<String, MessagingProvider> providers;

    public MessagingBrokerFactory(List<MessagingProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(p -> p.getName().toLowerCase(), p -> p));
        log.info("Initialized MessagingBrokerFactory with registered providers: {}", providers.keySet());
    }

    public MessagingProvider getProvider(String providerName) {
        if (providerName == null || providerName.isBlank()) {
            throw new IllegalArgumentException("Messaging provider name must not be empty");
        }
        MessagingProvider provider = providers.get(providerName.trim().toLowerCase());
        if (provider == null) {
            throw new IllegalArgumentException(
                    "Unknown messaging provider: '" + providerName + "'. Available providers: " + providers.keySet()
            );
        }
        if (!provider.isAvailable()) {
            throw new IllegalStateException("Messaging provider '" + providerName + "' is not available or not enabled in this bead");
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
