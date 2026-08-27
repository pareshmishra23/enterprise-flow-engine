package com.efe.traderecon.module;

import com.efe.traderecon.ikasan.builder.BuilderFactory;
import com.efe.traderecon.ikasan.builder.ModuleBuilder;
import com.efe.traderecon.ikasan.model.IkasanFlow;
import com.efe.traderecon.ikasan.model.IkasanModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ModuleConfiguration {

    @Bean
    public IkasanModule tradeReconModule(
            BuilderFactory builderFactory,
            @Value("${esb.module-name:trade-recon-esb}") String moduleName,
            @Value("${esb.description:Trade Reconciliation Enterprise Service Bus}") String description,
            @Qualifier("tradeIngestionFlow") IkasanFlow ingestionFlow,
            @Qualifier("reconciliationDispatchFlow") IkasanFlow dispatchFlow,
            @Qualifier("reconciliationProcessingFlow") IkasanFlow processingFlow) {

        ModuleBuilder moduleBuilder = builderFactory.getModuleBuilder(moduleName)
                .withDescription(description);

        moduleBuilder.addFlow(ingestionFlow);
        moduleBuilder.addFlow(dispatchFlow);
        moduleBuilder.addFlow(processingFlow);

        return moduleBuilder.build();
    }
}
