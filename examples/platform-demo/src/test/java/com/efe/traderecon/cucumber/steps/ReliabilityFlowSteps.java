package com.efe.traderecon.cucumber.steps;

import com.efe.traderecon.flow.reliabilitydemo.ReliabilityDemoFlowConfiguration;
import com.efe.traderecon.flow.reliabilitydemo.ReliabilityMessage;
import com.efe.traderecon.ikasan.model.FlowState;
import com.efe.traderecon.ikasan.model.IkasanFlow;
import com.efe.traderecon.ikasan.ui.FlowWiretapStore;
import com.efe.traderecon.reliability.DeadLetterQueue;
import com.efe.traderecon.reliability.ReliabilityAuditTrail;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.assertj.core.api.Assertions.assertThat;

public class ReliabilityFlowSteps {

    @Autowired
    @Qualifier("reliabilityDemoFlow")
    private IkasanFlow flow;

    @Autowired
    private ReliabilityDemoFlowConfiguration.ReliabilityDemoProcessor processor;

    @Autowired
    private ReliabilityDemoFlowConfiguration.ReliabilityDemoProducer producer;

    @Autowired
    private DeadLetterQueue deadLetterQueue;

    @Autowired
    private ReliabilityAuditTrail auditTrail;

    @Autowired
    private FlowWiretapStore wiretapStore;

    private ReliabilityMessage submittedMessage;
    private boolean messageProcessed;

    @Given("the reliability demo flow is running")
    public void theReliabilityDemoFlowIsRunning() {
        assertThat(flow).isNotNull();
        if (flow.getState() != FlowState.RUNNING) {
            flow.start();
        }
        assertThat(flow.getState()).isEqualTo(FlowState.RUNNING);
    }

    @Given("the reliability demo producer store is cleared")
    public void theReliabilityDemoProducerStoreIsCleared() {
        producer.clear();
        deadLetterQueue.clear();
        auditTrail.clear();
        wiretapStore.clear();
        processor.reset();
        messageProcessed = false;
    }

    @Given("a reliability message {string}")
    public void aReliabilityMessage(String id) {
        submittedMessage = new ReliabilityMessage(id, "content-" + id);
    }

    @Given("a permanently failing reliability message {string}")
    public void aPermanentlyFailingReliabilityMessage(String id) {
        ReliabilityMessage message = new ReliabilityMessage(id, "content-" + id);
        message.setFailingPermanent(true);
        submittedMessage = message;
    }

    @When("the reliability demo flow processes the message")
    public void theReliabilityDemoFlowProcessesTheMessage() {
        try {
            flow.onConsumerEvent(submittedMessage);
            messageProcessed = true;
        } catch (Exception e) {
            messageProcessed = false;
        }
    }

    @Then("the message should be marked processed")
    public void theMessageShouldBeMarkedProcessed() {
        assertThat(messageProcessed).isTrue();
        assertThat(submittedMessage.isProcessed()).isTrue();
    }

    @And("the message should be attempted {int} times")
    public void theMessageShouldBeAttemptedTimes(int attempts) {
        assertThat(submittedMessage.getAttemptsTaken()).isEqualTo(attempts);
    }

    @And("the dead letter queue should be empty")
    public void theDeadLetterQueueShouldBeEmpty() {
        assertThat(deadLetterQueue.size()).isZero();
    }

    @And("the reliability audit trail should record a successful outcome")
    public void theReliabilityAuditTrailShouldRecordASuccessfulOutcome() {
        assertThat(auditTrail.snapshot().stream().anyMatch(r -> "SUCCESS".equals(r.status()))).isTrue();
    }

    @Then("the message should not be processed")
    public void theMessageShouldNotBeProcessed() {
        assertThat(messageProcessed).isFalse();
        assertThat(submittedMessage.isProcessed()).isFalse();
    }

    @And("the dead letter queue should contain exactly {int} record")
    public void theDeadLetterQueueShouldContainExactlyRecord(int count) {
        assertThat(deadLetterQueue.size()).isEqualTo(count);
    }

    @And("the reliability audit trail should record a DLQ outcome")
    public void theReliabilityAuditTrailShouldRecordADlqOutcome() {
        assertThat(auditTrail.snapshot().stream().anyMatch(r -> "DLQ".equals(r.status()))).isTrue();
    }

    @And("the wiretap store should have recorded the message")
    public void theWiretapStoreShouldHaveRecordedTheMessage() {
        assertThat(wiretapStore.snapshot()).isNotEmpty();
        assertThat(wiretapStore.snapshot().stream()
                .anyMatch(w -> w.payload().contains(submittedMessage.getMessageId()))).isTrue();
    }
}
