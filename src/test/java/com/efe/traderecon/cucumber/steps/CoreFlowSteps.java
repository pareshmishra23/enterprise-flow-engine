package com.efe.traderecon.cucumber.steps;

import com.efe.traderecon.flow.core.EfeCoreEvent;
import com.efe.traderecon.flow.core.EfeCoreFlowConfiguration;
import com.efe.traderecon.ikasan.model.FlowState;
import com.efe.traderecon.ikasan.model.IkasanFlow;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CoreFlowSteps {

    @Autowired
    @Qualifier("efeCoreFlow")
    private IkasanFlow coreFlow;

    @Autowired
    private EfeCoreFlowConfiguration.EfeCoreEventConsumer consumer;

    @Autowired
    private EfeCoreFlowConfiguration.EfeMatchProducer matchProducer;

    @Autowired
    private EfeCoreFlowConfiguration.EfeBreakProducer breakProducer;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, Object> currentEventMap;
    private boolean validationFailed = false;
    private EfeCoreEvent lastProcessedEvent;

    @Given("the EFE core flow is running")
    public void theEfeCoreFlowIsRunning() {
        assertThat(coreFlow).isNotNull();
        if (coreFlow.getState() != FlowState.RUNNING) {
            coreFlow.start();
        }
        assertThat(coreFlow.getState()).isEqualTo(FlowState.RUNNING);
    }

    @Given("the match and break output stores are empty")
    public void theMatchAndBreakOutputStoresAreEmpty() {
        matchProducer.clear();
        breakProducer.clear();
        validationFailed = false;
        lastProcessedEvent = null;
    }

    @Given("I submit an event with expected quantity {double} and actual quantity {double}")
    public void iSubmitAnEventWithExpectedQuantityAndActualQuantity(double expected, double actual) {
        currentEventMap = new HashMap<>();
        currentEventMap.put("eventId", "E-" + System.currentTimeMillis());
        currentEventMap.put("type", "TRADE");
        currentEventMap.put("expectedQuantity", expected);
        currentEventMap.put("actualQuantity", actual);
    }

    @Given("I submit an event without an eventId")
    public void iSubmitAnEventWithoutAnEventId() {
        currentEventMap = new HashMap<>();
        currentEventMap.put("type", "TRADE");
        currentEventMap.put("expectedQuantity", 100.0);
        currentEventMap.put("actualQuantity", 100.0);
    }

    @When("the event is processed by the EFE core flow")
    public void theEventIsProcessedByTheEfeCoreFlow() {
        try {
            String json = objectMapper.writeValueAsString(currentEventMap);
            consumer.publish(json);
        } catch (Exception e) {
            validationFailed = true;
        }
    }

    @Then("the event should be classified as {string}")
    public void theEventShouldBeClassifiedAs(String expectedStatus) {
        if ("MATCH".equalsIgnoreCase(expectedStatus)) {
            assertThat(matchProducer.getMatchStore()).isNotEmpty();
            lastProcessedEvent = matchProducer.getMatchStore().get(0);
            assertThat(lastProcessedEvent.getStatus()).isEqualTo("MATCH");
        } else if ("BREAK".equalsIgnoreCase(expectedStatus)) {
            assertThat(breakProducer.getBreakStore()).isNotEmpty();
            lastProcessedEvent = breakProducer.getBreakStore().get(0);
            assertThat(lastProcessedEvent.getStatus()).isEqualTo("BREAK");
        }
    }

    @And("the event should be received by the {string} producer")
    public void theEventShouldBeReceivedByTheProducer(String producerName) {
        if ("EFE-MATCH-OUT".equalsIgnoreCase(producerName)) {
            assertThat(matchProducer.getMatchStore()).isNotEmpty();
        } else if ("EFE-BREAK-OUT".equalsIgnoreCase(producerName)) {
            assertThat(breakProducer.getBreakStore()).isNotEmpty();
        }
    }

    @And("the event should not be received by the {string} producer")
    public void theEventShouldNotBeReceivedByTheProducer(String producerName) {
        if ("EFE-MATCH-OUT".equalsIgnoreCase(producerName)) {
            assertThat(matchProducer.getMatchStore()).isEmpty();
        } else if ("EFE-BREAK-OUT".equalsIgnoreCase(producerName)) {
            assertThat(breakProducer.getBreakStore()).isEmpty();
        }
    }

    @Then("the event should fail validation")
    public void theEventShouldFailValidation() {
        assertThat(validationFailed).isTrue();
    }

    @And("no match or break output should be produced")
    public void noMatchOrBreakOutputShouldBeProduced() {
        assertThat(matchProducer.getMatchStore()).isEmpty();
        assertThat(breakProducer.getBreakStore()).isEmpty();
    }
}
