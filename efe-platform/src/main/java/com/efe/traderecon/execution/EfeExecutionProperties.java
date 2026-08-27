package com.efe.traderecon.execution;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "efe.execution")
public class EfeExecutionProperties {

    private Workers workers = new Workers();

    public Workers getWorkers() { return workers; }
    public void setWorkers(Workers workers) { this.workers = workers; }

    public static class Workers {
        private int coreSize = 4;
        private int maxSize = 10;
        private int queueCapacity = 100;

        public int getCoreSize() { return coreSize; }
        public void setCoreSize(int coreSize) { this.coreSize = coreSize; }

        public int getMaxSize() { return maxSize; }
        public void setMaxSize(int maxSize) { this.maxSize = maxSize; }

        public int getQueueCapacity() { return queueCapacity; }
        public void setQueueCapacity(int queueCapacity) { this.queueCapacity = queueCapacity; }
    }
}
