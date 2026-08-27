package com.efe.traderecon.persistence.postgres;

import com.efe.traderecon.persistence.spi.*;
import org.springframework.stereotype.Component;

/**
 * Future PostgreSQL Enterprise Persistence Provider Boundary.
 * Enables production relational storage with ACID guarantees and high throughput indexing.
 */
@Component
public class PostgresPersistenceProvider implements PersistenceProvider {

    @Override
    public String getName() {
        return "postgres";
    }

    @Override
    public JobRepository getJobRepository() {
        throw new UnsupportedOperationException("PostgreSQL persistence provider is a future enterprise target.");
    }

    @Override
    public TaskRepository getTaskRepository() {
        throw new UnsupportedOperationException("PostgreSQL persistence provider is a future enterprise target.");
    }

    @Override
    public TradeRepository getTradeRepository() {
        throw new UnsupportedOperationException("PostgreSQL persistence provider is a future enterprise target.");
    }

    @Override
    public ResultRepository getResultRepository() {
        throw new UnsupportedOperationException("PostgreSQL persistence provider is a future enterprise target.");
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
