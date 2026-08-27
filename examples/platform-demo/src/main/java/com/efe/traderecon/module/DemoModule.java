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
 * The EFE Platform Demonstration application's module. Wires the pure platform demo flows that
 * demonstrate reusable EFE capabilities: core flow (consumer/converter/translator/processor/router/
 * producer), foundation flows (incl. router and scheduled), async execution, intelligence audit and
 * reliability. This application depends only on efe-platform and proves the platform is consumable
 * by a standalone external application.
 */
@Configuration
public class DemoModule {

    @Bean
    public IkasanModule platformDemoModule(
            BuilderFactory builderFactory,
            @Value("${esb.module-name:efe-platform-demo}") String moduleName,
            @Value("${esb.description:EFE Platform Demonstration Module}") String description,
            @Qualifier("efeCoreFlow") IkasanFlow coreFlow,
            @Qualifier("efeAsyncFlow") IkasanFlow asyncFlow,
            @Qualifier("efeFoundationFlow") IkasanFlow foundationFlow,
            @Qualifier("efeScheduledFoundationFlow") IkasanFlow scheduledFoundationFlow,
            @Qualifier("efeRouterFoundationFlow") IkasanFlow routerFoundationFlow,
            @Qualifier("intelligenceAuditFlow") IkasanFlow intelligenceAuditFlow,
            @Qualifier("asyncDemoFlow") IkasanFlow asyncDemoFlow,
            @Qualifier("reliabilityDemoFlow") IkasanFlow reliabilityDemoFlow) {

        ModuleBuilder moduleBuilder = builderFactory.getModuleBuilder(moduleName)
                .withDescription(description);

        moduleBuilder.addFlow(coreFlow);
        moduleBuilder.addFlow(asyncFlow);
        moduleBuilder.addFlow(foundationFlow);
        moduleBuilder.addFlow(scheduledFoundationFlow);
        moduleBuilder.addFlow(routerFoundationFlow);
        moduleBuilder.addFlow(intelligenceAuditFlow);
        moduleBuilder.addFlow(asyncDemoFlow);
        moduleBuilder.addFlow(reliabilityDemoFlow);

        return moduleBuilder.build();
    }
}
