package com.efe.traderecon.ikasan.engine;

import com.efe.traderecon.ikasan.model.IkasanFlow;
import com.efe.traderecon.ikasan.model.IkasanModule;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IkasanEngine {
    private static final Logger log = LoggerFactory.getLogger(IkasanEngine.class);

    private final IkasanModule module;

    public IkasanEngine(IkasanModule module) {
        this.module = module;
    }

    @PostConstruct
    public void start() {
        log.info("Starting Ikasan Enterprise Integration Engine for module [{}]...", module.getName());
        module.start();
        log.info("Ikasan Enterprise Integration Engine is ACTIVE");
    }

    @PreDestroy
    public void stop() {
        log.info("Shutting down Ikasan Enterprise Integration Engine for module [{}]...", module.getName());
        module.stop();
        log.info("Ikasan Enterprise Integration Engine SHUTDOWN complete");
    }

    public IkasanModule getModule() {
        return module;
    }

    public Optional<IkasanFlow> getFlow(String flowName) {
        return module.getFlow(flowName);
    }
}
