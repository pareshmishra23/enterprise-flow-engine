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
            @Value("${esb.module-name:enterprise-flow-engine}") String moduleName,
            @Value("${esb.description:Enterprise Flow Engine Platform Runtime}") String description,
            @Qualifier("efeCoreFlow") IkasanFlow coreFlow,
            @Qualifier("efeAsyncFlow") IkasanFlow asyncFlow,
            @Qualifier("efeFoundationFlow") IkasanFlow foundationFlow,
            @Qualifier("efeScheduledFoundationFlow") IkasanFlow scheduledFoundationFlow,
            @Qualifier("efeRouterFoundationFlow") IkasanFlow routerFoundationFlow,
            @Qualifier("tradeIngestionFlow") IkasanFlow ingestionFlow,
            @Qualifier("reconciliationDispatchFlow") IkasanFlow dispatchFlow,
            @Qualifier("reconciliationProcessingFlow") IkasanFlow processingFlow,
            @Qualifier("intelligenceAuditFlow") IkasanFlow intelligenceAuditFlow,
            @Qualifier("asyncDemoFlow") IkasanFlow asyncDemoFlow,
            @Qualifier("dbDemoFlow") IkasanFlow dbDemoFlow,
            @Qualifier("reliabilityDemoFlow") IkasanFlow reliabilityDemoFlow) {

        ModuleBuilder moduleBuilder = builderFactory.getModuleBuilder(moduleName)
                .withDescription(description);

        moduleBuilder.addFlow(coreFlow);
        moduleBuilder.addFlow(asyncFlow);
        moduleBuilder.addFlow(foundationFlow);
        moduleBuilder.addFlow(scheduledFoundationFlow);
        moduleBuilder.addFlow(routerFoundationFlow);
        moduleBuilder.addFlow(ingestionFlow);
        moduleBuilder.addFlow(dispatchFlow);
        moduleBuilder.addFlow(processingFlow);
        moduleBuilder.addFlow(intelligenceAuditFlow);
        moduleBuilder.addFlow(asyncDemoFlow);
        moduleBuilder.addFlow(dbDemoFlow);
        moduleBuilder.addFlow(reliabilityDemoFlow);

        return moduleBuilder.build();
    }
}
