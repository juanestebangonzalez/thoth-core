package com.thoth.adapter.in.rest.exception;

import com.thoth.adapter.in.rest.controller.EquipmentController;
import com.thoth.adapter.in.rest.controller.MaintenanceController;
import com.thoth.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({EquipmentController.class, MaintenanceController.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testEquipmentEndpoint_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/equipment")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void testMaintenanceEndpoint_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/maintenance")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
}