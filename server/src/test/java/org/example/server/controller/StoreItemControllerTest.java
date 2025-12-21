package org.example.server.controller;

import org.example.server.dto.storeItem.StoreItemRequestDTO;
import org.example.server.dto.storeItem.StoreItemResponseDTO;
import org.example.server.service.StoreItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("StoreItemController Unit Tests")
class StoreItemControllerTest {

    @Mock
    private StoreItemService service;

    @InjectMocks
    private StoreItemController controller;

    private UUID itemId;
    private StoreItemRequestDTO request;
    private StoreItemResponseDTO response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        itemId = UUID.randomUUID();

        request = new StoreItemRequestDTO(
                "Toy Car",
                "Nice toy car",
                50,
                10
        );

        response = new StoreItemResponseDTO(
                itemId,
                "Toy Car",
                "Nice toy car",
                50,
                10
        );
    }

    // ---------------- LISTING ----------------

    @Test
    @DisplayName("getAllItems() - Should return all store items")
    void getAllItems_ReturnsItems() {
        when(service.getAllItems()).thenReturn(List.of(response));

        List<StoreItemResponseDTO> result = controller.getAllItems();

        assertEquals(1, result.size());
        verify(service).getAllItems();
    }

    @Test
    @DisplayName("getItemsSortedAsc() - Should return items sorted ascending")
    void getItemsSortedAsc_ReturnsSortedItems() {
        when(service.getItemsSortedAsc()).thenReturn(List.of(response));

        List<StoreItemResponseDTO> result = controller.getItemsSortedAsc();

        assertFalse(result.isEmpty());
        verify(service).getItemsSortedAsc();
    }

    @Test
    @DisplayName("getItemsSortedDesc() - Should return items sorted descending")
    void getItemsSortedDesc_ReturnsSortedItems() {
        when(service.getItemsSortedDesc()).thenReturn(List.of(response));

        List<StoreItemResponseDTO> result = controller.getItemsSortedDesc();

        assertFalse(result.isEmpty());
        verify(service).getItemsSortedDesc();
    }

    @Test
    @DisplayName("getItemsBelowPrice() - Should filter items below price")
    void getItemsBelowPrice_CallsServiceWithCorrectPrice() {
        controller.getItemsBelowPrice(100);

        verify(service).getItemsBelowPrice(100);
    }

    @Test
    @DisplayName("getItemsAbovePrice() - Should filter items above price")
    void getItemsAbovePrice_CallsServiceWithCorrectPrice() {
        controller.getItemsAbovePrice(30);

        verify(service).getItemsAbovePrice(30);
    }

    // ---------------- CRUD ----------------

    @Test
    @DisplayName("getItemById() - Should return item by ID")
    void getItemById_ReturnsItem() {
        when(service.getItemById(itemId)).thenReturn(response);

        StoreItemResponseDTO result = controller.getItemById(itemId);

        assertEquals(itemId, result.id());
        verify(service).getItemById(itemId);
    }

    @Test
    @DisplayName("createItem() - Should create new store item")
    void createItem_ReturnsCreatedItem() {
        when(service.createItem(any(StoreItemRequestDTO.class))).thenReturn(response);

        StoreItemResponseDTO result = controller.createItem(request);

        assertEquals("Toy Car", result.name());
        verify(service).createItem(request);
    }

    @Test
    @DisplayName("updateItem() - Should update existing item")
    void updateItem_ReturnsUpdatedItem() {
        when(service.updateItem(eq(itemId), any(StoreItemRequestDTO.class)))
                .thenReturn(response);

        StoreItemResponseDTO result = controller.updateItem(itemId, request);

        assertEquals(itemId, result.id());
        verify(service).updateItem(itemId, request);
    }

    @Test
    @DisplayName("patchItem() - Should partially update item")
    void patchItem_ReturnsPatchedItem() {
        when(service.patchItem(eq(itemId), any(StoreItemRequestDTO.class)))
                .thenReturn(response);

        StoreItemResponseDTO result = controller.patchItem(itemId, request);

        assertEquals(itemId, result.id());
        verify(service).patchItem(itemId, request);
    }

    @Test
    @DisplayName("deleteItem() - Should delete item")
    void deleteItem_DeletesItem() {
        doNothing().when(service).deleteItem(itemId);

        controller.deleteItem(itemId);

        verify(service).deleteItem(itemId);
    }

    // ---------------- STOCK ----------------

    @Test
    @DisplayName("restockItem() - Should restock item with given amount")
    void restockItem_ReturnsUpdatedItem() {
        when(service.restockItem(itemId, 5)).thenReturn(response);

        StoreItemResponseDTO result = controller.restockItem(itemId, 5);

        assertEquals(itemId, result.id());
        verify(service).restockItem(itemId, 5);
    }
}