package com.efe.traderecon.cucumber.steps;

import com.efe.traderecon.api.idempotency.IdempotencyStore;
import com.efe.traderecon.cucumber.support.ScenarioState;
import com.efe.traderecon.messaging.inmemory.InMemoryQueue;
import com.efe.traderecon.persistence.spi.JobRepository;
import com.efe.traderecon.persistence.spi.ResultRepository;
import com.efe.traderecon.persistence.spi.TaskRepository;
import com.efe.traderecon.persistence.spi.TradeRepository;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;

public class CommonHooks {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private IdempotencyStore idempotencyStore;

    @Autowired
    private InMemoryQueue inMemoryQueue;

    @Autowired
    private ScenarioState scenarioState;

    @Before
    public void resetEnvironment() {
        jobRepository.clear();
        taskRepository.clear();
        tradeRepository.clear();
        resultRepository.clear();
        idempotencyStore.clear();
        inMemoryQueue.clearAll();
        scenarioState.reset();
    }
}
