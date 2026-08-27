package com.efe.traderecon.intelligence.local;

import com.efe.traderecon.intelligence.spi.IntelligenceProvider;
import com.efe.traderecon.intelligence.spi.IntelligenceRequest;
import com.efe.traderecon.intelligence.spi.IntelligenceResult;
import com.efe.traderecon.intelligence.spi.IntelligenceType;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * EFE No-Op Intelligence Provider.
 *
 * When efe.intelligence.enabled=false, this provider handles ALL intelligence
 * types as a fallback, returning structured SKIPPED results.
 *
 * Order(100) ensures real providers take priority; this catches the remainder.
 */
@Component
@Order(100)
public class NoOpIntelligenceProvider implements IntelligenceProvider {

    @Override
    public boolean supports(IntelligenceType type) {
        // Accepts all types as a catch-all when AI is disabled
        return true;
    }

    @Override
    public String getProviderName() {
        return "no-op";
    }

    @Override
    public IntelligenceResult analyze(IntelligenceRequest request) {
        return IntelligenceResult.skipped(request.getIntelligenceType());
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
