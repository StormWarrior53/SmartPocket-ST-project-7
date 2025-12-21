package org.example.server.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.server.dto.auth.LoginRequestDTO;
import org.example.server.dto.auth.RegisterRequestDTO;
import org.example.server.repository.ParentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Authentication Integration Tests")
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ParentRepository parentRepository;

    @Test
    @DisplayName("POST /api/auth/register - Should register parent successfully")
    void registerParent_ShouldReturnParentData() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "parent1",
                "parent1@example.com",
                "Password123"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("parent1"))
                .andExpect(jsonPath("$.email").value("parent1@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should login and return JWT")
    void loginParent_ShouldReturnToken() throws Exception {
        // първо създаваме родител директно в DB
        RegisterRequestDTO register = new RegisterRequestDTO(
                "parent2",
                "parent2@example.com",
                "Password123"
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        LoginRequestDTO login = new LoginRequestDTO(
                "parent2@example.com",
                "Password123"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should fail for invalid data")
    void registerParent_InvalidData_ShouldReturn400() throws Exception {
        RegisterRequestDTO invalidRequest = new RegisterRequestDTO(
                "",          // missing username
                "invalid",   // invalid email
                "123"        // weak password
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}