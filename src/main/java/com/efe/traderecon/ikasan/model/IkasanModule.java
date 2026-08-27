package com.efe.traderecon.ikasan.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class IkasanModule {
    private static final Logger log = LoggerFactory.getLogger(IkasanModule.class);

    private final String name;
    private final String description;
    private final List<IkasanFlow> flows;
    private volatile boolean running = false;

    public IkasanModule(String name, String description, List<IkasanFlow> flows) {
        this.name = name;
        this.description = description;
        this.flows = flows != null ? new ArrayList<>(flows) : new ArrayList<>();
    }

    public synchronized void start() {
        log.info("Starting Ikasan Module [{}]", name);
        for (IkasanFlow flow : flows) {
            flow.start();
        }
        running = true;
        log.info("Ikasan Module [{}] successfully started with {} flows", name, flows.size());
    }

    public synchronized void stop() {
        log.info("Stopping Ikasan Module [{}]", name);
        for (IkasanFlow flow : flows) {
            flow.stop();
        }
        running = false;
        log.info("Ikasan Module [{}] stopped", name);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<IkasanFlow> getFlows() {
        return Collections.unmodifiableList(flows);
    }

    public Optional<IkasanFlow> getFlow(String flowName) {
        return flows.stream().filter(f -> f.getName().equalsIgnoreCase(flowName)).findFirst();
    }

    public boolean isRunning() {
        return running;
    }
}
