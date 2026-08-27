package com.efe.traderecon.persistence.mongodb;

import com.efe.traderecon.persistence.spi.*;
import org.springframework.stereotype.Component;

/**
 * Future MongoDB Document Persistence Provider Boundary.
 * Enables flexible document-oriented storage for complex unstructured trade datasets.
 */
@Component
public class MongoDbPersistenceProvider implements PersistenceProvider {

    @Override
    public String getName() {
        return "mongodb";
    }

    @Override
    public JobRepository getJobRepository() {
        throw new UnsupportedOperationException("MongoDB persistence provider is a future target.");
    }

    @Override
    public TaskRepository getTaskRepository() {
        throw new UnsupportedOperationException("MongoDB persistence provider is a future target.");
    }

    @Override
    public TradeRepository getTradeRepository() {
        throw new UnsupportedOperationException("MongoDB persistence provider is a future target.");
    }

    @Override
    public ResultRepository getResultRepository() {
        throw new UnsupportedOperationException("MongoDB persistence provider is a future target.");
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
