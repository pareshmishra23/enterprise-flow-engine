package com.efe.traderecon.ikasan;

import com.efe.traderecon.ikasan.engine.IkasanEngine;
import com.efe.traderecon.ikasan.model.FlowState;
import com.efe.traderecon.ikasan.model.IkasanFlow;
import com.efe.traderecon.ikasan.model.IkasanModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ModuleAndFlowsTest {

    @Autowired
    private IkasanEngine ikasanEngine;

    @Test
    @DisplayName("Should initialize the platform demo module with its demo flows running")
    void shouldInitializePlatformDemoModule() {
        IkasanModule module = ikasanEngine.getModule();
        assertThat(module).isNotNull();
        assertThat(module.getName()).isEqualTo("enterprise-flow-engine");
        assertThat(module.isRunning()).isTrue();
        assertThat(module.getFlows()).hasSize(8);

        Optional<IkasanFlow> coreFlow = module.getFlow("efe-core-flow");
        assertThat(coreFlow).isPresent();
        assertThat(coreFlow.get().getState()).isEqualTo(FlowState.RUNNING);
        assertThat(coreFlow.get().getConsumer()).isNotNull();

        Optional<IkasanFlow> foundationFlow = module.getFlow("efe-foundation-flow");
        assertThat(foundationFlow).isPresent();
        assertThat(foundationFlow.get().getState()).isEqualTo(FlowState.RUNNING);

        Optional<IkasanFlow> scheduledFoundationFlow = module.getFlow("efe-scheduled-foundation-flow");
        assertThat(scheduledFoundationFlow).isPresent();

        Optional<IkasanFlow> routerFoundationFlow = module.getFlow("efe-router-foundation-flow");
        assertThat(routerFoundationFlow).isPresent();

        Optional<IkasanFlow> asyncDemoFlow = module.getFlow("async-demo-flow");
        assertThat(asyncDemoFlow).isPresent();

        Optional<IkasanFlow> asyncFlow = module.getFlow("efe-async-flow");
        assertThat(asyncFlow).isPresent();

        Optional<IkasanFlow> intelligenceAuditFlow = module.getFlow("intelligence-audit-flow");
        assertThat(intelligenceAuditFlow).isPresent();

        Optional<IkasanFlow> reliabilityDemoFlow = module.getFlow("reliability-demo-flow");
        assertThat(reliabilityDemoFlow).isPresent();
        assertThat(reliabilityDemoFlow.get().getState()).isEqualTo(FlowState.RUNNING);
    }
}
