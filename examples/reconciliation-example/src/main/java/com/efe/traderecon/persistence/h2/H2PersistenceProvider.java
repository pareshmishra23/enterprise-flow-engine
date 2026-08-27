package com.efe.traderecon.persistence.h2;

import com.efe.traderecon.persistence.spi.*;
import org.springframework.stereotype.Component;

/**
 * Future H2 Relational Persistence Provider Boundary.
 * Enables embedded SQL persistence for module runtime and business entities.
 */
@Component
public class H2PersistenceProvider implements PersistenceProvider {

    @Override
    public String getName() {
        return "h2";
    }

    @Override
    public JobRepository getJobRepository() {
        throw new UnsupportedOperationException("H2 persistence provider will be activated in subsequent beads.");
    }

    @Override
    public TaskRepository getTaskRepository() {
        throw new UnsupportedOperationException("H2 persistence provider will be activated in subsequent beads.");
    }

    @Override
    public TradeRepository getTradeRepository() {
        throw new UnsupportedOperationException("H2 persistence provider will be activated in subsequent beads.");
    }

    @Override
    public ResultRepository getResultRepository() {
        throw new UnsupportedOperationException("H2 persistence provider will be activated in subsequent beads.");
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
