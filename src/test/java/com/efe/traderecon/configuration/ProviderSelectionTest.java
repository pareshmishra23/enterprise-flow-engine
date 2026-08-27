package com.efe.traderecon.configuration;

import com.efe.traderecon.messaging.spi.MessagingBrokerFactory;
import com.efe.traderecon.messaging.spi.MessagingProvider;
import com.efe.traderecon.persistence.spi.PersistenceProvider;
import com.efe.traderecon.persistence.spi.PersistenceProviderFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class ProviderSelectionTest {

    @Autowired
    private MessagingBrokerFactory messagingBrokerFactory;

    @Autowired
    private PersistenceProviderFactory persistenceProviderFactory;

    @Test
    @DisplayName("Should retrieve active inmemory messaging and persistence providers")
    void shouldRetrieveActiveProviders() {
        MessagingProvider messagingProvider = messagingBrokerFactory.getProvider("inmemory");
        assertThat(messagingProvider).isNotNull();
        assertThat(messagingProvider.getName()).isEqualTo("inmemory");
        assertThat(messagingProvider.isAvailable()).isTrue();

        PersistenceProvider persistenceProvider = persistenceProviderFactory.getProvider("inmemory");
        assertThat(persistenceProvider).isNotNull();
        assertThat(persistenceProvider.getName()).isEqualTo("inmemory");
        assertThat(persistenceProvider.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("Should throw clear error when unconfigured messaging provider is requested")
    void shouldFailOnUnknownMessagingProvider() {
        assertThatThrownBy(() -> messagingBrokerFactory.getProvider("kafka"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not available or not enabled in this bead");

        assertThatThrownBy(() -> messagingBrokerFactory.getProvider("non-existent-broker"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown messaging provider");
    }

    @Test
    @DisplayName("Should throw clear error when unconfigured persistence provider is requested")
    void shouldFailOnUnknownPersistenceProvider() {
        assertThatThrownBy(() -> persistenceProviderFactory.getProvider("postgres"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not available or not enabled in this bead");

        assertThatThrownBy(() -> persistenceProviderFactory.getProvider("unknown-db"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown persistence provider");
    }
}
