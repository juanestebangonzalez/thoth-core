package com.thoth.adapter.in.rest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regression tests for SEC-014: /api/v1/ai/** was fully permitAll(), letting
 * anyone trigger (likely cost-incurring) AI analysis for any equipment ID
 * without authentication, indirectly leaking equipment details and enabling
 * cost/DoS abuse.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AIControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void analyzeMaintenance_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/ai/maintenance/" + UUID.randomUUID()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void predictFailure_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/ai/predict-failure/" + UUID.randomUUID()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void recommendReplacement_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/ai/recommend-replacement/" + UUID.randomUUID()))
            .andExpect(status().isUnauthorized());
    }
}
