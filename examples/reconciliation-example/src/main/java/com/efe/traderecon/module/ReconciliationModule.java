package com.efe.traderecon.module;

import com.efe.traderecon.ikasan.builder.BuilderFactory;
import com.efe.traderecon.ikasan.builder.ModuleBuilder;
import com.efe.traderecon.ikasan.model.IkasanFlow;
import com.efe.traderecon.ikasan.model.IkasanModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The reconciliation-example reference application's EFE/Ikasan-aligned module. It wires exactly its
 * own three reconciliation flows: trade-ingestion-flow, reconciliation-dispatch-flow and
 * reconciliation-processing-flow. This demonstrates that an external business application can
 * define its own EFE Module and flows on top of the EFE Platform.
 */
@Configuration
public class ReconciliationModule {

    @Bean
    public IkasanModule reconciliationModuleDefinition(
            BuilderFactory builderFactory,
            @Value("${esb.module-name:trade-recon-esb}") String moduleName,
            @Value("${esb.description:Trade Reconciliation Reference Application}") String description,
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
