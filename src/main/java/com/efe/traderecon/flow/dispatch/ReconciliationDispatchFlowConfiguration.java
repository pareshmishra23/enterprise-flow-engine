package com.efe.traderecon.flow.dispatch;

import com.efe.traderecon.ikasan.builder.FlowBuilder;
import com.efe.traderecon.ikasan.model.IkasanFlow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReconciliationDispatchFlowConfiguration {

    public static final String FLOW_NAME = "reconciliation-dispatch-flow";

    @Bean
    public IkasanFlow reconciliationDispatchFlow(
            @Value("${esb.module-name:trade-recon-esb}") String moduleName,
            ScheduledTaskConsumer scheduledConsumer,
            TaskRetrievalBroker taskRetrievalBroker,
            TaskPreparationSplitter taskPreparationSplitter,
            MessagingDispatchProducer messagingProducer) {

        return new FlowBuilder(FLOW_NAME, moduleName)
                .consumer("Scheduled Dispatch Consumer", scheduledConsumer)
                .broker("Task Retrieval Broker", taskRetrievalBroker)
                .splitter("Task Preparation Splitter", taskPreparationSplitter)
                .producer("Messaging Dispatch Producer", messagingProducer)
                .build();
    }
}
