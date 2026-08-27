package com.efe.traderecon.flow.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EfeCoreComponentsTest {

    private EfeCoreFlowConfiguration.EfeCoreConverter converter;
    private EfeCoreFlowConfiguration.EfeCoreValidator validator;
    private EfeCoreFlowConfiguration.EfeCoreProcessor processor;
    private EfeCoreFlowConfiguration.EfeCoreRouter router;

    @BeforeEach
    void setUp() {
        converter = new EfeCoreFlowConfiguration.EfeCoreConverter();
        validator = new EfeCoreFlowConfiguration.EfeCoreValidator();
        processor = new EfeCoreFlowConfiguration.EfeCoreProcessor();
        router = new EfeCoreFlowConfiguration.EfeCoreRouter();
    }

    @Test
    void testConverterAndValidationSuccess() {
        String json = "{\"eventId\":\"E-1001\",\"type\":\"TRADE\",\"expectedQuantity\":100.0,\"actualQuantity\":100.0}";
        EfeCoreEvent event = converter.convert(json);
        assertThat(event).isNotNull();
        assertThat(event.getEventId()).isEqualTo("E-1001");

        EfeCoreEvent validated = validator.translate(event);
        assertThat(validated).isNotNull();
    }

    @Test
    void testValidationFailureMissingEventId() {
        EfeCoreEvent event = new EfeCoreEvent(null, null, "TRADE", 100.0, 100.0);
        assertThatThrownBy(() -> validator.translate(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("eventId is required");
    }

    @Test
    void testProcessorAndRoutingMatch() {
        EfeCoreEvent event = new EfeCoreEvent("E-1001", "CORR-01", "TRADE", 100.0, 100.0);
        EfeCoreEvent processed = processor.process(event);
        assertThat(processed.getStatus()).isEqualTo("MATCH");

        String route = router.route(processed);
        assertThat(route).isEqualTo("MATCH");
    }

    @Test
    void testProcessorAndRoutingBreak() {
        EfeCoreEvent event = new EfeCoreEvent("E-1002", "CORR-02", "TRADE", 100.0, 80.0);
        EfeCoreEvent processed = processor.process(event);
        assertThat(processed.getStatus()).isEqualTo("BREAK");

        String route = router.route(processed);
        assertThat(route).isEqualTo("BREAK");
    }
}
