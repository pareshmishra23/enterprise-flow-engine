package com.efe.traderecon.flow.intelligence;

import com.efe.traderecon.ikasan.builder.FlowBuilder;
import com.efe.traderecon.ikasan.model.IkasanFlow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * EFE Intelligence Audit Flow Configuration.
 *
 * Wires the intelligence-audit-flow into the Ikasan module as the 4th flow.
 *
 * Flow topology:
 *   IntelligenceEventConsumer (in-memory queue)
 *     → IntelligenceAnalysisBroker (LLM + Anomaly + Fraud + Aggregation)
 *     → IntelligenceResultProducerAdapter (audit log + future persistence)
 *
 * The consumer uses an in-memory blocking queue for EFE-003.
 * EFE-011 (Kafka) and EFE-013 (JMS) will supply real event-driven consumers.
 * EFE-008 (Persistence SPI) will wire real storage into the producer.
 */
@Configuration
public class IntelligenceAuditFlowConfiguration {

    public static final String FLOW_NAME = "intelligence-audit-flow";

    @Bean("intelligenceAuditFlow")
    public IkasanFlow intelligenceAuditFlow(
            @Value("${esb.module-name:trade-recon-esb}") String moduleName,
            IntelligenceEventConsumer eventConsumer,
            IntelligenceAnalysisBroker analysisBroker,
            IntelligenceResultProducerAdapter resultProducerAdapter) {

        return new FlowBuilder(FLOW_NAME, moduleName)
                .consumer("Intelligence Event Consumer", eventConsumer)
                .broker("Intelligence Analysis Broker", analysisBroker)
                .producer("Intelligence Result Producer", resultProducerAdapter)
                .build();
    }
}
