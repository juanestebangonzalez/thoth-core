package com.thoth.adapter.in.rest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Architecture regression: SedeController used to return SedeEntity (JPA)
 * directly. It must now return SedeDTO - this exercises the mapping
 * end-to-end (not just that it compiles).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SedeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void create_thenListActive_returnsDtoWithExpectedFields() throws Exception {
        String name = "Sede Central " + UUID.randomUUID();
        String payload = """
            {"name":"%s","address":"Calle 1","phone":"555-1234"}
            """.formatted(name);

        mockMvc.perform(post("/api/v1/sedes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value(name))
            .andExpect(jsonPath("$.address").value("Calle 1"))
            .andExpect(jsonPath("$.phone").value("555-1234"))
            .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(get("/api/v1/sedes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.name == '" + name + "')]").exists());
    }

    @Test
    void getById_withUnknownId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/sedes/" + UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }
}
