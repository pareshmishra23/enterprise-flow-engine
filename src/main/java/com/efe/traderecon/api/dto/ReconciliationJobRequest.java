package com.efe.traderecon.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReconciliationJobRequest {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate businessDate;

    private String source;

    private List<TradeRecordDto> records = new ArrayList<>();

    public ReconciliationJobRequest() {
    }

    public ReconciliationJobRequest(LocalDate businessDate, String source, List<TradeRecordDto> records) {
        this.businessDate = businessDate;
        this.source = source;
        this.records = records != null ? records : new ArrayList<>();
    }

    public LocalDate getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(LocalDate businessDate) {
        this.businessDate = businessDate;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<TradeRecordDto> getRecords() {
        return records;
    }

    public void setRecords(List<TradeRecordDto> records) {
        this.records = records;
    }

    @Override
    public String toString() {
        return "ReconciliationJobRequest{" +
                "businessDate=" + businessDate +
                ", source='" + source + '\'' +
                ", recordCount=" + (records != null ? records.size() : 0) +
                '}';
    }
}
