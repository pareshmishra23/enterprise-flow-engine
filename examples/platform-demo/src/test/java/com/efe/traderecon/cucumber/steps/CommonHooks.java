package com.efe.traderecon.cucumber.steps;

import com.efe.traderecon.cucumber.support.ScenarioState;
import com.efe.traderecon.ikasan.ui.FlowWiretapStore;
import com.efe.traderecon.messaging.inmemory.InMemoryQueue;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;

public class CommonHooks {

    @Autowired
    private InMemoryQueue inMemoryQueue;

    @Autowired
    private FlowWiretapStore wiretapStore;

    @Autowired
    private ScenarioState scenarioState;

    @Before
    public void resetEnvironment() {
        inMemoryQueue.clearAll();
        wiretapStore.clear();
        scenarioState.reset();
    }
}
