package com.efe.traderecon.flow.reliabilitydemo;

import java.io.Serializable;

/**
 * Payload for the reliability demo flow. Carries a stable message id used to
 * correlate audit/DLQ records across retry attempts.
 */
public class ReliabilityMessage implements Serializable {

    private String messageId;
    private String content;
    private int attemptsTaken;
    private boolean processed;
    private boolean failingPermanent;

    public ReliabilityMessage() {
    }

    public ReliabilityMessage(String messageId, String content) {
        this.messageId = messageId;
        this.content = content;
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getAttemptsTaken() { return attemptsTaken; }
    public void setAttemptsTaken(int attemptsTaken) { this.attemptsTaken = attemptsTaken; }

    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }

    public boolean isFailingPermanent() { return failingPermanent; }
    public void setFailingPermanent(boolean failingPermanent) { this.failingPermanent = failingPermanent; }

    public void markProcessed(int attempts) {
        this.processed = true;
        this.attemptsTaken = attempts;
    }

    @Override
    public String toString() {
        return "ReliabilityMessage{" +
                "messageId='" + messageId + '\'' +
                ", content='" + content + '\'' +
                ", attemptsTaken=" + attemptsTaken +
                ", processed=" + processed +
                '}';
    }
}
