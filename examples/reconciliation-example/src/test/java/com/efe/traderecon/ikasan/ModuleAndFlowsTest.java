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
    @DisplayName("Should initialize the Reconciliation module with its three flows running")
    void shouldInitializeModuleAndFlows() {
        IkasanModule module = ikasanEngine.getModule();
        assertThat(module).isNotNull();
        assertThat(module.getName()).isEqualTo("trade-recon-esb");
        assertThat(module.isRunning()).isTrue();
        assertThat(module.getFlows()).hasSize(3);

        Optional<IkasanFlow> ingestionFlow = module.getFlow("trade-ingestion-flow");
        assertThat(ingestionFlow).isPresent();
        assertThat(ingestionFlow.get().getState()).isEqualTo(FlowState.RUNNING);
        assertThat(ingestionFlow.get().getConsumer()).isNotNull();

        Optional<IkasanFlow> dispatchFlow = module.getFlow("reconciliation-dispatch-flow");
        assertThat(dispatchFlow).isPresent();
        assertThat(dispatchFlow.get().getState()).isEqualTo(FlowState.RUNNING);
        assertThat(dispatchFlow.get().getConsumer()).isNotNull();
        assertThat(dispatchFlow.get().getProducer()).isNotNull();

        Optional<IkasanFlow> processingFlow = module.getFlow("reconciliation-processing-flow");
        assertThat(processingFlow).isPresent();
        assertThat(processingFlow.get().getState()).isEqualTo(FlowState.RUNNING);
        assertThat(processingFlow.get().getConsumer()).isNotNull();
        assertThat(processingFlow.get().getProducer()).isNotNull();
    }

    @Test
    @DisplayName("Should handle flow lifecycle start and stop correctly")
    void shouldHandleFlowLifecycle() {
        IkasanFlow flow = ikasanEngine.getFlow("trade-ingestion-flow").orElseThrow();
        assertThat(flow.getState()).isEqualTo(FlowState.RUNNING);

        flow.stop();
        assertThat(flow.getState()).isEqualTo(FlowState.STOPPED);
        assertThat(flow.getConsumer().isRunning()).isFalse();

        flow.start();
        assertThat(flow.getState()).isEqualTo(FlowState.RUNNING);
        assertThat(flow.getConsumer().isRunning()).isTrue();
    }
}
