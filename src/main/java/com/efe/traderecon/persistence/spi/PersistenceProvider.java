package com.efe.traderecon.persistence.spi;

public interface PersistenceProvider {
    String getName();
    JobRepository getJobRepository();
    TaskRepository getTaskRepository();
    TradeRepository getTradeRepository();
    ResultRepository getResultRepository();
    boolean isAvailable();
}
