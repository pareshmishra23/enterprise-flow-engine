package com.efe.traderecon.flow.intelligence;

import com.efe.traderecon.intelligence.aggregator.IntelligenceSummary;
import com.efe.traderecon.ikasan.model.IkasanProducer;
import org.springframework.stereotype.Component;

/**
 * EFE Intelligence Result Producer Adapter — Ikasan Producer for the intelligence-audit-flow.
 * Delegates to IntelligenceResultProducer for actual persistence/emission.
 */
@Component
public class IntelligenceResultProducerAdapter implements IkasanProducer<IntelligenceSummary> {

    private final IntelligenceResultProducer resultProducer;

    public IntelligenceResultProducerAdapter(IntelligenceResultProducer resultProducer) {
        this.resultProducer = resultProducer;
    }

    @Override
    public String getName() {
        return "intelligence-result-producer";
    }

    @Override
    public void produce(IntelligenceSummary summary) {
        if (summary != null) {
            resultProducer.produce(summary);
        }
    }
}
