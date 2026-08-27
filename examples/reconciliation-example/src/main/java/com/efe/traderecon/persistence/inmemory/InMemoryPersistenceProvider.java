package com.efe.traderecon.persistence.inmemory;

import com.efe.traderecon.persistence.spi.*;
import org.springframework.stereotype.Component;

@Component
public class InMemoryPersistenceProvider implements PersistenceProvider {

    private final JobRepository jobRepository;
    private final TaskRepository taskRepository;
    private final TradeRepository tradeRepository;
    private final ResultRepository resultRepository;

    public InMemoryPersistenceProvider(
            JobRepository jobRepository,
            TaskRepository taskRepository,
            TradeRepository tradeRepository,
            ResultRepository resultRepository) {
        this.jobRepository = jobRepository;
        this.taskRepository = taskRepository;
        this.tradeRepository = tradeRepository;
        this.resultRepository = resultRepository;
    }

    @Override
    public String getName() {
        return "inmemory";
    }

    @Override
    public JobRepository getJobRepository() {
        return jobRepository;
    }

    @Override
    public TaskRepository getTaskRepository() {
        return taskRepository;
    }

    @Override
    public TradeRepository getTradeRepository() {
        return tradeRepository;
    }

    @Override
    public ResultRepository getResultRepository() {
        return resultRepository;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
