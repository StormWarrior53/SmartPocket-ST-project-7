package org.example.server.controller;

import org.example.server.dto.ChildResponse;
import org.example.server.dto.CreateChildRequest;
import org.example.server.service.ChildService;
import org.example.server.util.AuthenticationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for child-related endpoints in ParentController.
 *
 * This test class demonstrates:
 * - Mocking service dependencies
 * - Testing controller responses and status codes
 * - Verifying service method calls
 * - Testing both success and error scenarios
 */
@DisplayName("ParentController - Child Endpoints Unit Tests")
class ChildControllerIntegrationTest {

    @Mock
    private ChildService childService;

    @Mock
    private AuthenticationUtil authenticationUtil;

    @InjectMocks
    private ParentController parentController;

    private UUID parentId;
    private UUID childId;
    private CreateChildRequest createChildRequest;
    private ChildResponse childResponse;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Setup test data
        parentId = UUID.randomUUID();
        childId = UUID.randomUUID();

        createChildRequest = new CreateChildRequest("Johnny", 10);

        childResponse = new ChildResponse(
                childId,
                "Johnny",
                10,
                0,      // xp
                0,      // pocketMoney
                0,      // allowanceMoney
                false,  // hasPattern
                parentId
        );
    }

    @Test
    @DisplayName("createChild() - Should create child successfully and return 201")
    void createChild_WithValidData_ReturnsCreated() {
        // Arrange
        when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
        when(childService.createChild(eq(parentId), any(CreateChildRequest.class)))
                .thenReturn(childResponse);

        // Act
        ResponseEntity<ChildResponse> response = parentController.createChild(createChildRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(childId, response.getBody().id());
        assertEquals("Johnny", response.getBody().name());
        assertEquals(10, response.getBody().age());
        assertEquals(0, response.getBody().xp());
        assertEquals(parentId, response.getBody().parentId());

        // Verify service methods were called
        verify(authenticationUtil, times(1)).getCurrentUserId();
        verify(childService, times(1)).createChild(eq(parentId), any(CreateChildRequest.class));
    }

    @Test
    @DisplayName("createChild() - Should use parent ID from authentication")
    void createChild_UsesAuthenticatedParentId() {
        // Arrange
        UUID authenticatedParentId = UUID.randomUUID();
        when(authenticationUtil.getCurrentUserId()).thenReturn(authenticatedParentId);
        when(childService.createChild(eq(authenticatedParentId), any(CreateChildRequest.class)))
                .thenReturn(childResponse);

        // Act
        parentController.createChild(createChildRequest);

        // Assert
        verify(childService).createChild(eq(authenticatedParentId), any(CreateChildRequest.class));
    }

    @Test
    @DisplayName("getChildren() - Should return list of children with 200 OK")
    void getChildren_ReturnsChildrenList() {
        // Arrange
        ChildResponse child1 = new ChildResponse(
                UUID.randomUUID(), "Alice", 10, 100, 50, 20, true, parentId
        );
        ChildResponse child2 = new ChildResponse(
                UUID.randomUUID(), "Bob", 12, 200, 75, 30, false, parentId
        );
        List<ChildResponse> children = Arrays.asList(child1, child2);

        when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
        when(childService.getChildrenByParent(parentId)).thenReturn(children);

        // Act
        ResponseEntity<List<ChildResponse>> response = parentController.getChildren();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Alice", response.getBody().get(0).name());
        assertEquals("Bob", response.getBody().get(1).name());

        verify(childService).getChildrenByParent(parentId);
    }

    @Test
    @DisplayName("getChildren() - Should return empty list when no children")
    void getChildren_NoChildren_ReturnsEmptyList() {
        // Arrange
        when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
        when(childService.getChildrenByParent(parentId)).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<List<ChildResponse>> response = parentController.getChildren();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("getChild() - Should return specific child by ID")
    void getChild_WithValidId_ReturnsChild() {
        // Arrange
        when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
        when(childService.getChildById(parentId, childId)).thenReturn(childResponse);

        // Act
        ResponseEntity<ChildResponse> response = parentController.getChild(childId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(childId, response.getBody().id());
        assertEquals("Johnny", response.getBody().name());

        verify(childService).getChildById(parentId, childId);
    }

    @Test
    @DisplayName("getChild() - Should verify parent ownership")
    void getChild_VerifiesParentOwnership() {
        // Arrange
        UUID authenticatedParentId = UUID.randomUUID();
        when(authenticationUtil.getCurrentUserId()).thenReturn(authenticatedParentId);
        when(childService.getChildById(authenticatedParentId, childId)).thenReturn(childResponse);

        // Act
        parentController.getChild(childId);

        // Assert
        verify(childService).getChildById(eq(authenticatedParentId), eq(childId));
    }

    @Test
    @DisplayName("deleteChild() - Should delete child and return 204 No Content")
    void deleteChild_WithValidId_ReturnsNoContent() {
        // Arrange
        when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
        doNothing().when(childService).deleteChild(parentId, childId);

        // Act
        ResponseEntity<Void> response = parentController.deleteChild(childId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(childService).deleteChild(parentId, childId);
    }

    @Test
    @DisplayName("deleteChild() - Should verify service is called with correct parameters")
    void deleteChild_CallsServiceWithCorrectParams() {
        // Arrange
        UUID testParentId = UUID.randomUUID();
        UUID testChildId = UUID.randomUUID();

        when(authenticationUtil.getCurrentUserId()).thenReturn(testParentId);
        doNothing().when(childService).deleteChild(testParentId, testChildId);

        // Act
        parentController.deleteChild(testChildId);

        // Assert
        verify(authenticationUtil).getCurrentUserId();
        verify(childService).deleteChild(testParentId, testChildId);
        verifyNoMoreInteractions(childService);
    }

    @Test
    @DisplayName("resetPattern() - Should reset child pattern and return updated child")
    void resetPattern_WithValidId_ReturnsUpdatedChild() {
        // Arrange
        ChildResponse updatedChild = new ChildResponse(
                childId, "Johnny", 10, 0, 0, 0, false, parentId
        );

        when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
        when(childService.resetPattern(parentId, childId)).thenReturn(updatedChild);

        // Act
        ResponseEntity<ChildResponse> response = parentController.resetPattern(childId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().hasPattern());

        verify(childService).resetPattern(parentId, childId);
    }

    @Test
    @DisplayName("createChild() - Should return child with correct initial values")
    void createChild_ReturnsChildWithInitialValues() {
        // Arrange
        when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
        when(childService.createChild(any(UUID.class), any(CreateChildRequest.class)))
                .thenReturn(childResponse);

        // Act
        ResponseEntity<ChildResponse> response = parentController.createChild(createChildRequest);

        // Assert
        ChildResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(0, body.xp(), "Initial XP should be 0");
        assertEquals(0, body.pocketMoney(), "Initial pocket money should be 0");
        assertEquals(0, body.allowanceMoney(), "Initial allowance money should be 0");
        assertFalse(body.hasPattern(), "Initial pattern should be false");
    }

    @Test
    @DisplayName("Multiple operations - Should handle sequential calls correctly")
    void multipleOperations_HandleCorrectly() {
        // Arrange
        when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
        when(childService.createChild(any(UUID.class), any(CreateChildRequest.class)))
                .thenReturn(childResponse);
        when(childService.getChildById(parentId, childId)).thenReturn(childResponse);

        // Act
        ResponseEntity<ChildResponse> createResponse = parentController.createChild(createChildRequest);
        ResponseEntity<ChildResponse> getResponse = parentController.getChild(childId);

        // Assert
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());

        verify(childService).createChild(any(UUID.class), any(CreateChildRequest.class));
        verify(childService).getChildById(parentId, childId);
        verify(authenticationUtil, times(2)).getCurrentUserId();
    }

    @Test
    @DisplayName("updateChild() - Should update child successfully")
    void updateChild_WithValidData_ReturnsUpdatedChild() {
        // Arrange
        UpdateChildRequest updateRequest = new UpdateChildRequest("Johnny Updated", 11);

        ChildResponse updatedChild = new ChildResponse(
                childId,
                "Johnny Updated",
                11,
                0,
                0,
                0,
                false,
                parentId
        );

        when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
        when(childService.updateChild(parentId, childId, updateRequest))
                .thenReturn(updatedChild);

        // Act
        ResponseEntity<ChildResponse> response =
                parentController.updateChild(childId, updateRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Johnny Updated", response.getBody().name());
        assertEquals(11, response.getBody().age());

        verify(childService).updateChild(parentId, childId, updateRequest);
    }

    @Test
    @DisplayName("getChildInventory() - Should return inventory items for child")
    void getChildInventory_ReturnsInventoryList() {
        // Arrange
        InventoryItemResponse item1 = new InventoryItemResponse(
                UUID.randomUUID(), "Toy Car", 1
        );
        InventoryItemResponse item2 = new InventoryItemResponse(
                UUID.randomUUID(), "Book", 2
        );

        List<InventoryItemResponse> inventory = List.of(item1, item2);

        when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
        when(inventoryService.getChildInventoryForParent(parentId, childId))
                .thenReturn(inventory);

        // Act
        ResponseEntity<List<InventoryItemResponse>> response =
                parentController.getChildInventory(childId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Toy Car", response.getBody().get(0).itemName());

        verify(inventoryService)
                .getChildInventoryForParent(parentId, childId);
    }

    @Test
    @DisplayName("addMoneyToChild() - Should add money and return updated child")
    void addMoneyToChild_WithValidAmount_ReturnsUpdatedChild() {
        // Arrange
        AddMoneyRequest request = new AddMoneyRequest(50);

        ChildResponse updatedChild = new ChildResponse(
                childId,
                "Johnny",
                10,
                0,
                50,
                0,
                false,
                parentId
        );

        when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
        when(childService.addMoneyToChild(parentId, childId, 50))
                .thenReturn(updatedChild);

        // Act
        ResponseEntity<ChildResponse> response =
                parentController.addMoneyToChild(childId, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(50, response.getBody().pocketMoney());

        verify(childService)
                .addMoneyToChild(parentId, childId, 50);
    }
}