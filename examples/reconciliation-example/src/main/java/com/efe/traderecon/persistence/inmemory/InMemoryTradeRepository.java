package com.efe.traderecon.persistence.inmemory;

import com.efe.traderecon.domain.Trade;
import com.efe.traderecon.persistence.spi.TradeRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryTradeRepository implements TradeRepository {

    // Key is "jobId:tradeId"
    private final ConcurrentHashMap<String, Trade> storage = new ConcurrentHashMap<>();

    private String key(String tradeId, String jobId) {
        return (jobId != null ? jobId : "") + ":" + (tradeId != null ? tradeId : "");
    }

    @Override
    public Trade save(Trade trade) {
        if (trade == null || trade.getTradeId() == null || trade.getJobId() == null) {
            throw new IllegalArgumentException("Trade, tradeId, and jobId cannot be null");
        }
        storage.put(key(trade.getTradeId(), trade.getJobId()), trade);
        return trade;
    }

    @Override
    public List<Trade> saveAll(List<Trade> trades) {
        if (trades == null) return new ArrayList<>();
        trades.forEach(this::save);
        return trades;
    }

    @Override
    public Optional<Trade> findById(String tradeId, String jobId) {
        if (tradeId == null || jobId == null) return Optional.empty();
        return Optional.ofNullable(storage.get(key(tradeId, jobId)));
    }

    @Override
    public List<Trade> findByJobId(String jobId) {
        if (jobId == null) return new ArrayList<>();
        return storage.values().stream()
                .filter(t -> jobId.equals(t.getJobId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Trade> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void clear() {
        storage.clear();
    }
}
