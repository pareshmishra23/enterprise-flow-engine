package com.efe.traderecon.processor;

import com.efe.traderecon.domain.Task;
import com.efe.traderecon.domain.TaskResult;
import com.efe.traderecon.domain.TaskStatus;
import com.efe.traderecon.domain.Trade;
import com.efe.traderecon.persistence.inmemory.InMemoryTradeRepository;
import com.efe.traderecon.processor.impl.TradeReconciliationProcessor;
import com.efe.traderecon.processor.spi.TaskProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TradeReconciliationProcessorTest {

    private InMemoryTradeRepository tradeRepository;
    private TradeReconciliationProcessor processor;
    private TaskProcessorRegistry registry;

    @BeforeEach
    void setUp() {
        tradeRepository = new InMemoryTradeRepository();
        processor = new TradeReconciliationProcessor(tradeRepository);
        registry = new TaskProcessorRegistry(List.of(processor));
    }

    @Test
    @DisplayName("Should resolve processor and execute reconciliation matching logic")
    void shouldResolveAndExecuteProcessor() {
        TaskProcessor resolved = registry.getProcessor("TRADE_RECONCILIATION");
        assertThat(resolved).isNotNull();
        assertThat(resolved.supports("TRADE_RECONCILIATION")).isTrue();

        String jobId = "JOB-T1";
        tradeRepository.saveAll(List.of(
                new Trade("TR-1", jobId, "ACC-1", "AAPL", new BigDecimal("100"), new BigDecimal("150")),
                new Trade("TR-2", jobId, "ACC-1", "MSFT", new BigDecimal("0"), new BigDecimal("300")) // break
        ));

        Task task = new Task("TSK-T1", jobId, "TRADE_RECONCILIATION");
        TaskResult result = resolved.process(task);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(result.getMatchedCount()).isEqualTo(1);
        assertThat(result.getBreakCount()).isEqualTo(1);
        assertThat(result.getResults()).hasSize(2);
    }
}
