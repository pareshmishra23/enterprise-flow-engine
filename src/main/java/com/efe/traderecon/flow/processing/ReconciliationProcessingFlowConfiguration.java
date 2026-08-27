package com.efe.traderecon.flow.processing;

import com.efe.traderecon.ikasan.builder.FlowBuilder;
import com.efe.traderecon.ikasan.model.IkasanFlow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReconciliationProcessingFlowConfiguration {

    public static final String FLOW_NAME = "reconciliation-processing-flow";

    @Bean
    public IkasanFlow reconciliationProcessingFlow(
            @Value("${esb.module-name:trade-recon-esb}") String moduleName,
            MessagingProcessingConsumer messagingConsumer,
            TaskEventTranslator taskEventTranslator,
            TaskProcessingBroker taskProcessingBroker,
            ResultPersistenceBroker resultPersistenceBroker,
            ProcessingResultProducer processingResultProducer) {

        return new FlowBuilder(FLOW_NAME, moduleName)
                .consumer("Messaging Task Consumer", messagingConsumer)
                .converter("Task Message Event Translator", taskEventTranslator)
                .broker("Business Task Processing Broker", taskProcessingBroker)
                .broker("Reconciliation Result Persistence Broker", resultPersistenceBroker)
                .producer("Processing Result Producer", processingResultProducer)
                .build();
    }
}
