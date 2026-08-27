package com.efe.traderecon.persistence.spi;

import com.efe.traderecon.domain.Trade;

import java.util.List;
import java.util.Optional;

public interface TradeRepository {
    Trade save(Trade trade);
    List<Trade> saveAll(List<Trade> trades);
    Optional<Trade> findById(String tradeId, String jobId);
    List<Trade> findByJobId(String jobId);
    List<Trade> findAll();
    void clear();
}
