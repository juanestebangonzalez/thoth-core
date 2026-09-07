package com.thoth.adapter.in.rest.controller;

import com.thoth.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MaintenanceController.class)
@Import(SecurityConfig.class)
class MaintenanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testListMaintenance_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/maintenance")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void testGetMaintenance_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/maintenance/550e8400-e29b-41d4-a716-446655440000")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
}