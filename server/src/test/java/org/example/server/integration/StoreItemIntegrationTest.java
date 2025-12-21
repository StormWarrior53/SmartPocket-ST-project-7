package org.example.server.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.server.dto.storeItem.StoreItemRequestDTO;
import org.example.server.model.StoreItem;
import org.example.server.repository.StoreItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("StoreItem Integration Tests")
class StoreItemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StoreItemRepository repository;

    @Test
    @DisplayName("POST /api/store - Should create store item")
    void createItem_ShouldReturnCreatedItem() throws Exception {
        StoreItemRequestDTO request = new StoreItemRequestDTO(
                "Puzzle",
                "Kids puzzle",
                40,
                5
        );

        mockMvc.perform(post("/api/store")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Puzzle"))
                .andExpect(jsonPath("$.price").value(40))
                .andExpect(jsonPath("$.stock").value(5));
    }

    @Test
    @DisplayName("GET /api/store - Should return all items")
    void getAllItems_ShouldReturnList() throws Exception {
        repository.save(new StoreItem(
                UUID.randomUUID(),
                "Ball",
                "Football",
                30,
                10
        ));

        mockMvc.perform(get("/api/store"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/store/below/{price} - Should filter by price")
    void getItemsBelowPrice_ShouldWork() throws Exception {
        repository.save(new StoreItem(
                UUID.randomUUID(),
                "Cheap Toy",
                "Cheap",
                10,
                5
        ));

        mockMvc.perform(get("/api/store/below/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price", lessThan(20)));
    }

    @Test
    @DisplayName("POST /api/store/{id}/restock - Should increase stock")
    void restockItem_ShouldIncreaseStock() throws Exception {
        StoreItem item = repository.save(new StoreItem(
                UUID.randomUUID(),
                "Car",
                "Toy car",
                50,
                2
        ));

        mockMvc.perform(post("/api/store/{id}/restock?amount=3", item.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(5));
    }
}