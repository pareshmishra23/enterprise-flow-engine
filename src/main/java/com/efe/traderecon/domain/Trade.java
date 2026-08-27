package com.efe.traderecon.domain;

import java.math.BigDecimal;
import java.util.Objects;

public class Trade {
    private String tradeId;
    private String jobId;
    private String accountId;
    private String securityId;
    private BigDecimal quantity;
    private BigDecimal price;

    public Trade() {
    }

    public Trade(String tradeId, String jobId, String accountId, String securityId, BigDecimal quantity, BigDecimal price) {
        this.tradeId = tradeId;
        this.jobId = jobId;
        this.accountId = accountId;
        this.securityId = securityId;
        this.quantity = quantity;
        this.price = price;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getSecurityId() {
        return securityId;
    }

    public void setSecurityId(String securityId) {
        this.securityId = securityId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trade trade = (Trade) o;
        return Objects.equals(tradeId, trade.tradeId) && Objects.equals(jobId, trade.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tradeId, jobId);
    }

    @Override
    public String toString() {
        return "Trade{" +
                "tradeId='" + tradeId + '\'' +
                ", jobId='" + jobId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", securityId='" + securityId + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}
