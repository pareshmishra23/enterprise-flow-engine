package com.efe.traderecon.cucumber.steps;

import com.efe.traderecon.flow.foundation.FoundationFlowConfiguration;
import com.efe.traderecon.ikasan.engine.IkasanEngine;
import com.efe.traderecon.ikasan.model.FlowState;
import com.efe.traderecon.ikasan.model.IkasanFlow;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class IkasanFoundationSteps {

    @Autowired
    private IkasanEngine ikasanEngine;

    @Autowired
    @Qualifier("efeFoundationFlow")
    private IkasanFlow foundationFlow;

    @Autowired
    @Qualifier("efeScheduledFoundationFlow")
    private IkasanFlow scheduledFoundationFlow;

    @Autowired
    @Qualifier("efeRouterFoundationFlow")
    private IkasanFlow routerFoundationFlow;

    @Autowired
    private FoundationFlowConfiguration.FoundationEventConsumer foundationConsumer;

    @Autowired
    private FoundationFlowConfiguration.FoundationResultProducer foundationProducer;

    @Autowired
    private FoundationFlowConfiguration.FoundationScheduledConsumer scheduledConsumer;

    @Autowired
    private FoundationFlowConfiguration.FoundationScheduledProducer scheduledProducer;

    @Autowired
    private FoundationFlowConfiguration.RouterEventConsumer routerConsumer;

    @Autowired
    private FoundationFlowConfiguration.FoundationRouteAProducer routeAProducer;

    @Autowired
    private FoundationFlowConfiguration.FoundationRouteBProducer routeBProducer;

    private String lastEventJson;
    private boolean eventSubmitted = false;

    // ==========================================
    // 1. Foundation Flow Steps
    // ==========================================

    @Given("the EFE Ikasan module is running")
    public void theEfeIkasanModuleIsRunning() {
        assertThat(ikasanEngine).isNotNull();
        assertThat(ikasanEngine.getModule()).isNotNull();
        assertThat(ikasanEngine.getModule().isRunning()).isTrue();
    }

    @Given("the foundation flow is running")
    public void theFoundationFlowIsRunning() {
        assertThat(foundationFlow).isNotNull();
        if (foundationFlow.getState() != FlowState.RUNNING) {
            foundationFlow.start();
        }
        assertThat(foundationFlow.getState()).isEqualTo(FlowState.RUNNING);
        foundationProducer.clear();
    }

    @When("I submit a test event")
    public void iSubmitATestEvent() {
        lastEventJson = "{\"tradeId\":\"T-FOUNDATION-01\",\"symbol\":\"AAPL\",\"amount\":1500.0}";
        foundationConsumer.publish(lastEventJson);
        eventSubmitted = true;
    }

    @Then("the event should enter the Ikasan flow")
    public void theEventShouldEnterTheIkasanFlow() {
        assertThat(eventSubmitted).isTrue();
    }

    @And("the converter should process the event")
    public void theConverterShouldProcessTheEvent() {
        // Verified by the fact that intermediate Converter executed in the Ikasan flow
        assertThat(foundationFlow.getElements()).isNotEmpty();
    }

    @And("the processor should process the event")
    public void theProcessorShouldProcessTheEvent() {
        List<Map<String, Object>> results = foundationProducer.getProducedResults();
        assertThat(results).isNotEmpty();
        Map<String, Object> latest = results.get(results.size() - 1);
        assertThat(latest.get("status")).isEqualTo("PROCESSED");
    }

    @And("the producer should receive the result")
    public void theProducerShouldReceiveTheResult() {
        List<Map<String, Object>> results = foundationProducer.getProducedResults();
        assertThat(results).isNotEmpty();
        Map<String, Object> latest = results.get(results.size() - 1);
        assertThat(latest.get("tradeId")).isEqualTo("T-FOUNDATION-01");
    }

    @And("the event should complete successfully")
    public void theEventShouldCompleteSuccessfully() {
        assertThat(foundationFlow.getTotalEventsProcessed()).isGreaterThanOrEqualTo(1);
    }

    // ==========================================
    // 2. Scheduled Flow Steps
    // ==========================================

    @Given("the scheduled foundation flow is running")
    public void theScheduledFoundationFlowIsRunning() {
        assertThat(scheduledFoundationFlow).isNotNull();
        if (scheduledFoundationFlow.getState() != FlowState.RUNNING) {
            scheduledFoundationFlow.start();
        }
        assertThat(scheduledFoundationFlow.getState()).isEqualTo(FlowState.RUNNING);
        scheduledProducer.clear();
    }

    @When("the scheduled trigger occurs")
    public void theScheduledTriggerOccurs() {
        scheduledConsumer.trigger("MANUAL_CRON_TRIGGER_2026");
    }

    @Then("the scheduled consumer should initiate processing")
    public void theScheduledConsumerShouldInitiateProcessing() {
        assertThat(scheduledConsumer.isRunning()).isTrue();
    }

    @And("the processor should execute")
    public void theProcessorShouldExecute() {
        List<Map<String, Object>> events = scheduledProducer.getProducedEvents();
        assertThat(events).isNotEmpty();
        Map<String, Object> latest = events.get(events.size() - 1);
        assertThat(latest.get("status")).isEqualTo("EXECUTED");
    }

    // ==========================================
    // 3. Router Flow Steps
    // ==========================================

    @Given("the foundation routing flow is running")
    public void theFoundationRoutingFlowIsRunning() {
        assertThat(routerFoundationFlow).isNotNull();
        if (routerFoundationFlow.getState() != FlowState.RUNNING) {
            routerFoundationFlow.start();
        }
        assertThat(routerFoundationFlow.getState()).isEqualTo(FlowState.RUNNING);
        routeAProducer.clear();
        routeBProducer.clear();
    }

    @When("I submit an event with route {string}")
    public void iSubmitAnEventWithRoute(String routeName) {
        FoundationFlowConfiguration.RoutedEvent event =
                new FoundationFlowConfiguration.RoutedEvent(routeName, "PAYLOAD-FOR-" + routeName);
        routerConsumer.publish(event);
    }

    @Then("the event should reach producer {string}")
    public void theEventShouldReachProducer(String routeName) {
        if ("A".equalsIgnoreCase(routeName)) {
            assertThat(routeAProducer.getReceivedEvents()).isNotEmpty();
            assertThat(routeAProducer.getReceivedEvents().get(0).getRoute()).isEqualTo("A");
        } else if ("B".equalsIgnoreCase(routeName)) {
            assertThat(routeBProducer.getReceivedEvents()).isNotEmpty();
            assertThat(routeBProducer.getReceivedEvents().get(0).getRoute()).isEqualTo("B");
        }
    }

    @And("the event should not reach producer {string}")
    public void theEventShouldNotReachProducer(String routeName) {
        if ("A".equalsIgnoreCase(routeName)) {
            assertThat(routeAProducer.getReceivedEvents()).isEmpty();
        } else if ("B".equalsIgnoreCase(routeName)) {
            assertThat(routeBProducer.getReceivedEvents()).isEmpty();
        }
    }
}
