package com.efe.traderecon.module;

import com.efe.traderecon.ikasan.builder.BuilderFactory;
import com.efe.traderecon.ikasan.builder.ModuleBuilder;
import com.efe.traderecon.ikasan.model.IkasanFlow;
import com.efe.traderecon.ikasan.model.IkasanModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModuleConfiguration {

    @Bean
    public IkasanModule tradeReconModule(
            BuilderFactory builderFactory,
            @Value("${esb.module-name:trade-recon-esb}") String moduleName,
            @Value("${esb.description:Trade Reconciliation Enterprise Service Bus}") String description,
            @Qualifier("efeCoreFlow") IkasanFlow coreFlow,
            @Qualifier("efeFoundationFlow") IkasanFlow foundationFlow,
            @Qualifier("efeScheduledFoundationFlow") IkasanFlow scheduledFoundationFlow,
            @Qualifier("efeRouterFoundationFlow") IkasanFlow routerFoundationFlow,
            @Qualifier("tradeIngestionFlow") IkasanFlow ingestionFlow,
            @Qualifier("reconciliationDispatchFlow") IkasanFlow dispatchFlow,
            @Qualifier("reconciliationProcessingFlow") IkasanFlow processingFlow,
            @Qualifier("intelligenceAuditFlow") IkasanFlow intelligenceAuditFlow,
            @Qualifier("asyncDemoFlow") IkasanFlow asyncDemoFlow,
            @Qualifier("dbDemoFlow") IkasanFlow dbDemoFlow) {

        ModuleBuilder moduleBuilder = builderFactory.getModuleBuilder(moduleName)
                .withDescription(description);

        moduleBuilder.addFlow(coreFlow);
        moduleBuilder.addFlow(foundationFlow);
        moduleBuilder.addFlow(scheduledFoundationFlow);
        moduleBuilder.addFlow(routerFoundationFlow);
        moduleBuilder.addFlow(ingestionFlow);
        moduleBuilder.addFlow(dispatchFlow);
        moduleBuilder.addFlow(processingFlow);
        moduleBuilder.addFlow(intelligenceAuditFlow);
        moduleBuilder.addFlow(asyncDemoFlow);
        moduleBuilder.addFlow(dbDemoFlow);

        return moduleBuilder.build();
    }
}
