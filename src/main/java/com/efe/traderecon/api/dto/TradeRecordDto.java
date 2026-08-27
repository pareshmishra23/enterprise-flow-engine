package com.efe.traderecon.api.dto;

import java.math.BigDecimal;

public class TradeRecordDto {

    private String tradeId;
    private String accountId;
    private String securityId;
    private BigDecimal quantity;
    private BigDecimal price;

    public TradeRecordDto() {
    }

    public TradeRecordDto(String tradeId, String accountId, String securityId, BigDecimal quantity, BigDecimal price) {
        this.tradeId = tradeId;
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
    public String toString() {
        return "TradeRecordDto{" +
                "tradeId='" + tradeId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", securityId='" + securityId + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}
