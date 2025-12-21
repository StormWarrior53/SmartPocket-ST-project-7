package org.example.server.service;

import org.example.server.dto.storeItem.StoreItemRequestDTO;
import org.example.server.dto.storeItem.StoreItemResponseDTO;
import org.example.server.model.StoreItem;
import org.example.server.repository.StoreItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StoreItemService.
 *
 * Covers:
 * - CRUD operations
 * - Sorting & filtering
 * - Patch updates
 * - Restocking logic
 */
@DisplayName("StoreItemService - Business Logic Unit Tests")
class StoreItemServiceTest {

    @Mock
    private StoreItemRepository storeItemRepository;

    @InjectMocks
    private StoreItemService storeItemService;

    private UUID itemId;
    private StoreItem storeItem;
    private StoreItemRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        itemId = UUID.randomUUID();

        storeItem = new StoreItem();
        storeItem.setId(itemId);
        storeItem.setName("Toy Car");
        storeItem.setDescription("Small toy car");
        storeItem.setPrice(50);
        storeItem.setStock(10);

        requestDTO = new StoreItemRequestDTO(
                "Toy Car",
                "Small toy car",
                50,
                10
        );
    }

    // ---------- GET ALL ----------

    @Test
    @DisplayName("getAllItems() - Should return all store items")
    void getAllItems_ReturnsItems() {
        // Arrange
        when(storeItemRepository.findAll())
                .thenReturn(List.of(storeItem));

        // Act
        List<StoreItemResponseDTO> result =
                storeItemService.getAllItems();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Toy Car", result.get(0).name());

        verify(storeItemRepository).findAll();
    }

    // ---------- SORTING ----------

    @Test
    @DisplayName("getItemsSortedAsc() - Should return items sorted by price ascending")
    void getItemsSortedAsc_ReturnsSortedItems() {
        // Arrange
        when(storeItemRepository.findAllByOrderByPriceAsc())
                .thenReturn(List.of(storeItem));

        // Act
        List<StoreItemResponseDTO> result =
                storeItemService.getItemsSortedAsc();

        // Assert
        assertFalse(result.isEmpty());
        verify(storeItemRepository).findAllByOrderByPriceAsc();
    }

    @Test
    @DisplayName("getItemsSortedDesc() - Should return items sorted by price descending")
    void getItemsSortedDesc_ReturnsSortedItems() {
        // Arrange
        when(storeItemRepository.findAllByOrderByPriceDesc())
                .thenReturn(List.of(storeItem));

        // Act
        List<StoreItemResponseDTO> result =
                storeItemService.getItemsSortedDesc();

        // Assert
        assertFalse(result.isEmpty());
        verify(storeItemRepository).findAllByOrderByPriceDesc();
    }

    // ---------- FILTERING ----------

    @Test
    @DisplayName("getItemsBelowPrice() - Should return items below given price")
    void getItemsBelowPrice_ReturnsItems() {
        // Arrange
        when(storeItemRepository.findByPriceLessThan(100))
                .thenReturn(List.of(storeItem));

        // Act
        List<StoreItemResponseDTO> result =
                storeItemService.getItemsBelowPrice(100);

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.get(0).price() < 100);

        verify(storeItemRepository).findByPriceLessThan(100);
    }

    @Test
    @DisplayName("getItemsAbovePrice() - Should return items above given price")
    void getItemsAbovePrice_ReturnsItems() {
        // Arrange
        when(storeItemRepository.findByPriceGreaterThan(10))
                .thenReturn(List.of(storeItem));

        // Act
        List<StoreItemResponseDTO> result =
                storeItemService.getItemsAbovePrice(10);

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.get(0).price() > 10);

        verify(storeItemRepository).findByPriceGreaterThan(10);
    }

    // ---------- GET BY ID ----------

    @Test
    @DisplayName("getItemById() - Should return item when found")
    void getItemById_Found_ReturnsItem() {
        // Arrange
        when(storeItemRepository.findById(itemId))
                .thenReturn(Optional.of(storeItem));

        // Act
        StoreItemResponseDTO result =
                storeItemService.getItemById(itemId);

        // Assert
        assertNotNull(result);
        assertEquals(itemId, result.id());

        verify(storeItemRepository).findById(itemId);
    }

    @Test
    @DisplayName("getItemById() - Should throw exception when not found")
    void getItemById_NotFound_ThrowsException() {
        // Arrange
        when(storeItemRepository.findById(itemId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> storeItemService.getItemById(itemId));
    }

    // ---------- CREATE ----------

    @Test
    @DisplayName("createItem() - Should save and return new item")
    void createItem_ValidRequest_SavesItem() {
        // Arrange
        when(storeItemRepository.save(any(StoreItem.class)))
                .thenReturn(storeItem);

        // Act
        StoreItemResponseDTO result =
                storeItemService.createItem(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Toy Car", result.name());

        verify(storeItemRepository).save(any(StoreItem.class));
    }

    // ---------- UPDATE ----------

    @Test
    @DisplayName("updateItem() - Should update existing item")
    void updateItem_ExistingItem_UpdatesSuccessfully() {
        // Arrange
        StoreItemRequestDTO updateRequest = new StoreItemRequestDTO(
                "Updated Toy",
                "Updated desc",
                60,
                5
        );

        when(storeItemRepository.findById(itemId))
                .thenReturn(Optional.of(storeItem));
        when(storeItemRepository.save(any(StoreItem.class)))
                .thenReturn(storeItem);

        // Act
        StoreItemResponseDTO result =
                storeItemService.updateItem(itemId, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Toy", result.name());

        verify(storeItemRepository).findById(itemId);
        verify(storeItemRepository).save(any(StoreItem.class));
    }

    @Test
    @DisplayName("updateItem() - Should throw exception when item not found")
    void updateItem_NotFound_ThrowsException() {
        // Arrange
        when(storeItemRepository.findById(itemId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> storeItemService.updateItem(itemId, requestDTO));

        verify(storeItemRepository, never()).save(any());
    }

    // ---------- PATCH ----------

    @Test
    @DisplayName("patchItem() - Should update only provided fields")
    void patchItem_PartialUpdate_UpdatesCorrectly() {
        // Arrange
        StoreItemRequestDTO patchRequest = new StoreItemRequestDTO(
                null,
                null,
                70,
                null
        );

        when(storeItemRepository.findById(itemId))
                .thenReturn(Optional.of(storeItem));
        when(storeItemRepository.save(any(StoreItem.class)))
                .thenReturn(storeItem);

        // Act
        StoreItemResponseDTO result =
                storeItemService.patchItem(itemId, patchRequest);

        // Assert
        assertNotNull(result);
        assertEquals(70, result.price());

        verify(storeItemRepository).save(any(StoreItem.class));
    }

    // ---------- DELETE ----------

    @Test
    @DisplayName("deleteItem() - Should delete item when exists")
    void deleteItem_ExistingItem_DeletesSuccessfully() {
        // Arrange
        when(storeItemRepository.existsById(itemId))
                .thenReturn(true);

        // Act
        storeItemService.deleteItem(itemId);

        // Assert
        verify(storeItemRepository).deleteById(itemId);
    }

    @Test
    @DisplayName("deleteItem() - Should throw exception when item not found")
    void deleteItem_NotFound_ThrowsException() {
        // Arrange
        when(storeItemRepository.existsById(itemId))
                .thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> storeItemService.deleteItem(itemId));

        verify(storeItemRepository, never()).deleteById(any());
    }

    // ---------- RESTOCK ----------

    @Test
    @DisplayName("restockItem() - Should increase stock correctly")
    void restockItem_ValidAmount_IncreasesStock() {
        // Arrange
        when(storeItemRepository.findById(itemId))
                .thenReturn(Optional.of(storeItem));
        when(storeItemRepository.save(any(StoreItem.class)))
                .thenReturn(storeItem);

        // Act
        StoreItemResponseDTO result =
                storeItemService.restockItem(itemId, 5);

        // Assert
        assertNotNull(result);
        assertEquals(15, result.stock());

        verify(storeItemRepository).save(any(StoreItem.class));
    }
}