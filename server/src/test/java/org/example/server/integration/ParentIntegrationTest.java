package org.example.server.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.server.dto.parent.ParentProfileDTO;
import org.example.server.dto.parent.UpdateParentProfileDTO;
import org.example.server.dto.child.AddMoneyRequest;
import org.example.server.model.Parent;
import org.example.server.repository.ParentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("ParentController Integration Tests")
class ParentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ParentRepository parentRepository;

    @Test
    @DisplayName("GET /api/parents/me - Should return parent profile")
    @WithMockUser(username = "parent1")
    void getParentProfile_ShouldReturnProfile() throws Exception {
        Parent parent = parentRepository.save(new Parent(
                UUID.randomUUID(),
                "parent1",
                "parent1@example.com",
                "Password123"
        ));

        mockMvc.perform(get("/api/parents/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(parent.getUsername())))
                .andExpect(jsonPath("$.email", is(parent.getEmail())));
    }

    @Test
    @DisplayName("PUT /api/parents/me - Should update parent profile")
    @WithMockUser(username = "parent2")
    void updateParentProfile_ShouldReturnUpdatedProfile() throws Exception {
        Parent parent = parentRepository.save(new Parent(
                UUID.randomUUID(),
                "parent2",
                "parent2@example.com",
                "Password123"
        ));

        UpdateParentProfileDTO updateRequest = new UpdateParentProfileDTO(
                "newParent2",
                "newemail@example.com"
        );

        mockMvc.perform(put("/api/parents/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("newParent2")))
                .andExpect(jsonPath("$.email", is("newemail@example.com")));
    }

    @Test
    @DisplayName("POST /api/parents/me/add-money - Should add money to child")
    @WithMockUser(username = "parent3")
    void addMoneyToChild_ShouldIncreaseMoney() throws Exception {
        // Създаваме родител и дете
        Parent parent = parentRepository.save(new Parent(
                UUID.randomUUID(),
                "parent3",
                "parent3@example.com",
                "Password123"
        ));

        UUID childId = UUID.randomUUID(); // това трябва да съответства на child в DB
        AddMoneyRequest addMoneyRequest = new AddMoneyRequest(50);

        // endpoint: /api/parents/me/children/{childId}/add-money
        mockMvc.perform(post("/api/parents/me/children/{childId}/add-money", childId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addMoneyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pocketMoney").value(50));
    }

    @Test
    @DisplayName("GET /api/parents/me/children/{childId}/inventory - Should return child inventory")
    @WithMockUser(username = "parent4")
    void getChildInventory_ShouldReturnList() throws Exception {
        UUID childId = UUID.randomUUID();

        mockMvc.perform(get("/api/parents/me/children/{childId}/inventory", childId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}