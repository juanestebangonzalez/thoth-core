package com.thoth.adapter.in.rest.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoth.application.service.UserService;
import com.thoth.adapter.out.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SedeControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserJpaRepository userRepository;
    @Autowired private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String registerAdminAndGetToken() throws Exception {
        String username = "sede_admin_" + UUID.randomUUID().toString().substring(0, 8);
        String payload = """
            {"username":"%s","password":"Sup3rSecret!","email":"%s@example.com"}
            """.formatted(username, username);

        String body = mockMvc.perform(post("/api/v1/auth/register")
                .header("X-Forwarded-For", randomFakeIp())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(body).get("token").asText();

        // Promote to ADMIN since new users get USER role
        var user = userRepository.findByUsername(username).orElseThrow();
        userService.changeRole(user.getId(), "ADMIN");

        return token;
    }

    @Test
    void create_thenListActive_returnsDtoWithExpectedFields() throws Exception {
        String token = registerAdminAndGetToken();
        String name = "Sede Central " + UUID.randomUUID();
        String payload = """
            {"name":"%s","address":"Calle 1","phone":"555-1234"}
            """.formatted(name);

        mockMvc.perform(post("/api/v1/sedes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value(name))
            .andExpect(jsonPath("$.address").value("Calle 1"))
            .andExpect(jsonPath("$.phone").value("555-1234"))
            .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(get("/api/v1/sedes")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.name == '" + name + "')]").exists());
    }

    @Test
    void getById_withUnknownId_returns404() throws Exception {
        String token = registerAdminAndGetToken();

        mockMvc.perform(get("/api/v1/sedes/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound());
    }

    private String randomFakeIp() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return "10." + r.nextInt(1, 255) + "." + r.nextInt(1, 255) + "." + r.nextInt(1, 255);
    }
}
