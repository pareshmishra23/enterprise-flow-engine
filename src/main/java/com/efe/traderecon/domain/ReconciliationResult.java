package com.efe.traderecon.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class ReconciliationResult {
    private String resultId;
    private String jobId;
    private String taskId;
    private String tradeId;
    private DifferenceType differenceType;
    private BigDecimal differenceAmount;
    private String comment;
    private Instant createdAt;

    public ReconciliationResult() {
        this.createdAt = Instant.now();
    }

    public ReconciliationResult(String resultId, String jobId, String taskId, String tradeId, DifferenceType differenceType, BigDecimal differenceAmount) {
        this();
        this.resultId = resultId;
        this.jobId = jobId;
        this.taskId = taskId;
        this.tradeId = tradeId;
        this.differenceType = differenceType;
        this.differenceAmount = differenceAmount;
    }

    public String getResultId() {
        return resultId;
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public DifferenceType getDifferenceType() {
        return differenceType;
    }

    public void setDifferenceType(DifferenceType differenceType) {
        this.differenceType = differenceType;
    }

    public BigDecimal getDifferenceAmount() {
        return differenceAmount;
    }

    public void setDifferenceAmount(BigDecimal differenceAmount) {
        this.differenceAmount = differenceAmount;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReconciliationResult that = (ReconciliationResult) o;
        return Objects.equals(resultId, that.resultId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resultId);
    }

    @Override
    public String toString() {
        return "ReconciliationResult{" +
                "resultId='" + resultId + '\'' +
                ", jobId='" + jobId + '\'' +
                ", taskId='" + taskId + '\'' +
                ", tradeId='" + tradeId + '\'' +
                ", differenceType=" + differenceType +
                ", differenceAmount=" + differenceAmount +
                ", createdAt=" + createdAt +
                '}';
    }
}
