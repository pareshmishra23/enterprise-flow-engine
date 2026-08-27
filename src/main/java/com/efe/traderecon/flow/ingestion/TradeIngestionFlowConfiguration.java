package com.efe.traderecon.flow.ingestion;

import com.efe.traderecon.ikasan.builder.FlowBuilder;
import com.efe.traderecon.ikasan.model.IkasanFlow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TradeIngestionFlowConfiguration {

    public static final String FLOW_NAME = "trade-ingestion-flow";

    @Bean
    public IkasanFlow tradeIngestionFlow(
            @Value("${esb.module-name:trade-recon-esb}") String moduleName,
            RestConsumer restConsumer,
            ReconciliationJobJsonConverter converter,
            ValidationTranslator validationTranslator,
            JobRegistrationBroker jobRegistrationBroker,
            JobRegistrationResponseProducer responseProducer) {

        return new FlowBuilder(FLOW_NAME, moduleName)
                .consumer("REST Entry Consumer", restConsumer)
                .converter("JSON Request Converter", converter)
                .translator("Validation & Normalization Translator", validationTranslator)
                .broker("Job Registration Broker", jobRegistrationBroker)
                .producer("HTTP 201 Response Producer", responseProducer)
                .build();
    }
}
