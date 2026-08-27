package com.efe.traderecon.configuration;

import com.efe.traderecon.persistence.spi.PersistenceProvider;
import com.efe.traderecon.persistence.spi.PersistenceProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PersistenceConfiguration {
    private static final Logger log = LoggerFactory.getLogger(PersistenceConfiguration.class);

    private final PersistenceProviderFactory factory;
    private final String providerName;

    public PersistenceConfiguration(
            PersistenceProviderFactory factory,
            @Value("${persistence.provider:inmemory}") String providerName) {
        this.factory = factory;
        this.providerName = providerName;
        // Eagerly validate configured persistence provider
        PersistenceProvider provider = factory.getProvider(providerName);
        log.info("Initialized active persistence provider: [{}]", provider.getName());
    }
}
