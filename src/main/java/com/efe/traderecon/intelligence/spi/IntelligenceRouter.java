package com.efe.traderecon.intelligence.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * EFE Intelligence Router.
 * Dispatches an IntelligenceRequest to the correct IntelligenceProvider
 * based on the requested IntelligenceType.
 *
 * This is the only EFE component that knows about routing logic.
 * Ikasan flow components call this; they do not select providers directly.
 */
@Component
public class IntelligenceRouter {

    private static final Logger log = LoggerFactory.getLogger(IntelligenceRouter.class);

    private final IntelligenceRegistry registry;

    public IntelligenceRouter(IntelligenceRegistry registry) {
        this.registry = registry;
    }

    public IntelligenceResult route(IntelligenceRequest request) {
        log.info("Routing intelligence request [{}] of type [{}]",
                request.getRequestId(), request.getIntelligenceType());

        long start = System.currentTimeMillis();
        try {
            IntelligenceProvider provider = registry.getProvider(request.getIntelligenceType());
            IntelligenceResult result = provider.analyze(request);
            result.setProcessingTimeMs(System.currentTimeMillis() - start);
            result.setRequestId(request.getRequestId());
            result.setCorrelationId(request.getCorrelationId());
            result.setTimestamp(Instant.now());

            log.info("Intelligence routing complete [{}]: decision={}, success={}, errorCode={}",
                    request.getRequestId(), result.getDecision(), result.isSuccess(), result.getErrorCode());
            return result;

        } catch (Exception e) {
            log.error("Intelligence routing failed for request [{}]: {}", request.getRequestId(), e.getMessage(), e);
            IntelligenceResult err = IntelligenceResult.error(
                    request.getIntelligenceType(),
                    "AI_PROVIDER_ERROR",
                    new ModelMetadata("unknown", "unknown", "unknown", "unknown"));
            err.setProcessingTimeMs(System.currentTimeMillis() - start);
            err.setRequestId(request.getRequestId());
            err.setCorrelationId(request.getCorrelationId());
            err.setExplanation(e.getMessage());
            return err;
        }
    }
}
