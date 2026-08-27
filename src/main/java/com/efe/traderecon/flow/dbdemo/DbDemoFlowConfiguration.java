package com.efe.traderecon.flow.dbdemo;

import com.efe.traderecon.domain.Job;
import com.efe.traderecon.domain.JobStatus;
import com.efe.traderecon.domain.JobType;
import com.efe.traderecon.ikasan.builder.FlowBuilder;
import com.efe.traderecon.ikasan.model.IkasanBroker;
import com.efe.traderecon.ikasan.model.IkasanConsumer;
import com.efe.traderecon.ikasan.model.IkasanFlow;
import com.efe.traderecon.ikasan.model.IkasanProducer;
import com.efe.traderecon.persistence.spi.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Consumer;

@Configuration
public class DbDemoFlowConfiguration {

    public static final String FLOW_NAME = "db-demo-flow";

    @Bean("dbDemoFlow")
    public IkasanFlow dbDemoFlow(
            @Value("${esb.module-name:trade-recon-esb}") String moduleName,
            DbScheduledConsumer consumer,
            DatabaseAccessBroker broker,
            DbResultProducer producer) {

        return new FlowBuilder(FLOW_NAME, moduleName)
                .consumer("DB Scheduled Consumer", consumer)
                .broker("Database Access Broker", broker)
                .producer("DB Result Producer", producer)
                .build();
    }

    @Component
    public static class DbScheduledConsumer implements IkasanConsumer<String> {
        private volatile boolean running = false;
        private Consumer<String> listener;

        @Override public String getName() { return "db-scheduled-consumer"; }
        @Override public void start() { running = true; }
        @Override public void stop() { running = false; }
        @Override public boolean isRunning() { return running; }
        @Override public void setListener(Consumer<String> listener) { this.listener = listener; }

        public void trigger(String jobId) {
            if (listener != null) listener.accept(jobId);
        }
    }

    @Component
    public static class DatabaseAccessBroker implements IkasanBroker<String, Job> {
        private static final Logger log = LoggerFactory.getLogger(DatabaseAccessBroker.class);
        private final JobRepository jobRepository;

        public DatabaseAccessBroker(JobRepository jobRepository) {
            this.jobRepository = jobRepository;
        }

        @Override public String getName() { return "database-access-broker"; }

        @Override
        public Job invoke(String jobId) {
            log.info("DatabaseAccessBroker: querying/updating database for jobId={}", jobId);
            Optional<Job> existing = jobRepository.findById(jobId);
            if (existing.isPresent()) {
                Job j = existing.get();
                j.setStatus(JobStatus.IN_PROGRESS);
                return jobRepository.save(j);
            } else {
                Job newJob = new Job(jobId, JobType.RECONCILIATION, "DB_DEMO", LocalDate.now(), 0);
                newJob.setStatus(JobStatus.SUBMITTED);
                return jobRepository.save(newJob);
            }
        }
    }

    @Component
    public static class DbResultProducer implements IkasanProducer<Job> {
        private static final Logger log = LoggerFactory.getLogger(DbResultProducer.class);
        private volatile Job lastJob;

        @Override public String getName() { return "db-result-producer"; }

        @Override
        public void produce(Job job) {
            this.lastJob = job;
            log.info("DbResultProducer produced job: jobId={}, status={}", job.getJobId(), job.getStatus());
        }

        public Job getLastJob() { return lastJob; }
    }
}
