package com.efe.traderecon.api;

import com.efe.traderecon.api.controller.JobController;
import com.efe.traderecon.api.controller.ReconciliationJobController;
import com.efe.traderecon.api.dto.ReconciliationJobRequest;
import com.efe.traderecon.api.dto.TradeRecordDto;
import com.efe.traderecon.ikasan.model.IkasanFlow;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class ReconciliationJobControllerTest {

    @Autowired
    @Qualifier("tradeIngestionFlow")
    private IkasanFlow tradeIngestionFlow;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.efe.traderecon.ikasan.ui.IkasanDashboardController ikasanDashboardController;

    private MockMvc reconMockMvc;
    private MockMvc dashboardMockMvc;

    @BeforeEach
    void setUp() {
        ReconciliationJobController reconController = new ReconciliationJobController(tradeIngestionFlow);
        this.reconMockMvc = MockMvcBuilders.standaloneSetup(reconController).build();
        this.dashboardMockMvc = MockMvcBuilders.standaloneSetup(ikasanDashboardController).build();
    }

    @Test
    @DisplayName("Should submit job through REST API endpoint and receive HTTP 201 with Job ID")
    void shouldSubmitJobViaRest() throws Exception {
        ReconciliationJobRequest request = new ReconciliationJobRequest(
                LocalDate.of(2026, 8, 27),
                "CUSTODIAN",
                List.of(
                        new TradeRecordDto("TR-REST-1", "ACC-01", "AAPL", new BigDecimal("100.0"), new BigDecimal("150.0"))
                )
        );

        reconMockMvc.perform(post("/api/v1/jobs/reconciliation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobId", startsWith("JOB-")))
                .andExpect(jsonPath("$.status", is("SUBMITTED")))
                .andExpect(jsonPath("$.source", is("CUSTODIAN")))
                .andExpect(jsonPath("$.totalRecords", is(1)));
    }

    @Test
    @DisplayName("Should retrieve Ikasan module and flow hierarchy via dashboard API")
    void shouldRetrieveModuleDetails() throws Exception {
        dashboardMockMvc.perform(get("/api/v1/ikasan/module"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moduleName", is("trade-recon-esb")))
                .andExpect(jsonPath("$.running", is(true)))
                .andExpect(jsonPath("$.flows", hasSize(6)));
    }
}
