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

@WebMvcTest(EquipmentController.class)
@Import(SecurityConfig.class)
class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testListEquipment_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/equipment")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void testGetEquipment_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/equipment/550e8400-e29b-41d4-a716-446655440000")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void testDeleteEquipment_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/equipment/550e8400-e29b-41d4-a716-446655440000")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }
}