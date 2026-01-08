package org.example.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.server.dto.*;
import org.example.server.dto.inventory.InventoryItemResponse;
import org.example.server.service.ChildService;
import org.example.server.service.InventoryService;
import org.example.server.service.ParentService;
import org.example.server.util.AuthenticationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ParentController.
 *
 * Covers:
 * - Authentication endpoints (register, login, logout)
 * - Profile management
 * - Child management endpoints
 * - Child inventory access
 * - Money management
 */
@DisplayName("ParentController - Unit Tests")
class ParentControllerTest {

  @Mock
  private ParentService parentService;

  @Mock
  private ChildService childService;

  @Mock
  private InventoryService inventoryService;

  @Mock
  private AuthenticationUtil authenticationUtil;

  @Mock
  private HttpServletRequest httpServletRequest;

  @InjectMocks
  private ParentController parentController;

  private UUID parentId;
  private UUID childId;
  private String token;
  private AuthResponse authResponse;
  private ParentResponse parentResponse;
  private ChildResponse childResponse;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    parentId = UUID.randomUUID();
    childId = UUID.randomUUID();
    token = "Bearer test.jwt.token";

    authResponse = new AuthResponse(
        parentId,
        "parent@example.com",
        "John",
        "Doe",
        "parent",
        LocalDateTime.now(),
        "jwt.token",
        System.currentTimeMillis() + 86400000);

    parentResponse = new ParentResponse(
        parentId,
        "parent@example.com",
        "John",
        "Doe",
        LocalDateTime.now());

    childResponse = new ChildResponse(
        childId,
        "Alice",
        10,
        100,
        50,
        20,
        true,
        parentId);
  }

  // ---------- AUTHENTICATION ENDPOINTS ----------

  @Test
  @DisplayName("register() - Should register parent successfully")
  void register_ValidRequest_ReturnsCreated() {
    RegisterParentRequest request = new RegisterParentRequest(
        "new@example.com", "Jane", "Smith", "Password123");

    when(parentService.registerParent(request)).thenReturn(authResponse);

    ResponseEntity<AuthResponse> response = parentController.register(request);

    assertNotNull(response);
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(authResponse, response.getBody());

    verify(parentService).registerParent(request);
  }

  @Test
  @DisplayName("login() - Should login successfully")
  void login_ValidCredentials_ReturnsOk() {
    LoginRequest request = new LoginRequest("parent@example.com", "Password123");

    when(parentService.login(request)).thenReturn(authResponse);

    ResponseEntity<AuthResponse> response = parentController.login(request);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(authResponse, response.getBody());

    verify(parentService).login(request);
  }

  @Test
  @DisplayName("getMyProfile() - Should return parent profile")
  void getMyProfile_Authenticated_ReturnsProfile() {
    when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
    when(parentService.getParentProfile(parentId)).thenReturn(parentResponse);

    ResponseEntity<ParentResponse> response = parentController.getMyProfile();

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(parentResponse, response.getBody());

    verify(authenticationUtil).getCurrentUserId();
    verify(parentService).getParentProfile(parentId);
  }

  @Test
  @DisplayName("logout() - Should logout successfully with token")
  void logout_WithToken_ReturnsNoContent() {
    when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
    when(httpServletRequest.getHeader("Authorization")).thenReturn(token);
    doNothing().when(parentService).logout(eq(parentId), any(String.class));

    ResponseEntity<Void> response = parentController.logout(httpServletRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

    verify(authenticationUtil).getCurrentUserId();
    verify(httpServletRequest).getHeader("Authorization");
    verify(parentService).logout(eq(parentId), eq("test.jwt.token"));
  }

  @Test
  @DisplayName("logout() - Should handle missing authorization header")
  void logout_NoAuthHeader_ReturnsNoContent() {
    when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
    when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

    ResponseEntity<Void> response = parentController.logout(httpServletRequest);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

    verify(parentService, never()).logout(any(), any());
  }

  @Test
  @DisplayName("logout() - Should handle malformed authorization header")
  void logout_MalformedAuthHeader_ReturnsNoContent() {
    when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
    when(httpServletRequest.getHeader("Authorization")).thenReturn("InvalidFormat");

    ResponseEntity<Void> response = parentController.logout(httpServletRequest);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

    verify(parentService, never()).logout(any(), any());
  }

  // ---------- CHILD MANAGEMENT ENDPOINTS ----------

  @Test
  @DisplayName("createChild() - Should create child successfully")
  void createChild_ValidRequest_ReturnsCreated() {
    CreateChildRequest request = new CreateChildRequest("Bob", 8);

    when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
    when(childService.createChild(parentId, request)).thenReturn(childResponse);

    ResponseEntity<ChildResponse> response = parentController.createChild(request);

    assertNotNull(response);
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(childResponse, response.getBody());

    verify(authenticationUtil).getCurrentUserId();
    verify(childService).createChild(parentId, request);
  }

  @Test
  @DisplayName("getChildren() - Should return list of children")
  void getChildren_Authenticated_ReturnsList() {
    ChildResponse child2 = new ChildResponse(
        UUID.randomUUID(), "Charlie", 12, 200, 75, 30, false, parentId);
    List<ChildResponse> children = Arrays.asList(childResponse, child2);

    when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
    when(childService.getChildrenByParent(parentId)).thenReturn(children);

    ResponseEntity<List<ChildResponse>> response = parentController.getChildren();

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(2, response.getBody().size());
    assertEquals(children, response.getBody());

    verify(authenticationUtil).getCurrentUserId();
    verify(childService).getChildrenByParent(parentId);
  }

  @Test
  @DisplayName("getChild() - Should return specific child")
  void getChild_ValidChildId_ReturnsChild() {
    when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
    when(childService.getChildById(parentId, childId)).thenReturn(childResponse);

    ResponseEntity<ChildResponse> response = parentController.getChild(childId);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(childResponse, response.getBody());

    verify(authenticationUtil).getCurrentUserId();
    verify(childService).getChildById(parentId, childId);
  }

  @Test
  @DisplayName("updateChild() - Should update child successfully")
  void updateChild_ValidRequest_ReturnsUpdated() {
    UpdateChildRequest request = new UpdateChildRequest("Alice Updated", 11);
    ChildResponse updatedChild = new ChildResponse(
        childId, "Alice Updated", 11, 100, 50, 20, true, parentId);

    when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
    when(childService.updateChild(parentId, childId, request)).thenReturn(updatedChild);

    ResponseEntity<ChildResponse> response = parentController.updateChild(childId, request);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Alice Updated", response.getBody().name());
    assertEquals(11, response.getBody().age());

    verify(authenticationUtil).getCurrentUserId();
    verify(childService).updateChild(parentId, childId, request);
  }

  @Test
  @DisplayName("deleteChild() - Should delete child successfully")
  void deleteChild_ValidChildId_ReturnsNoContent() {
    when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
    doNothing().when(childService).deleteChild(parentId, childId);

    ResponseEntity<Void> response = parentController.deleteChild(childId);

    assertNotNull(response);
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

    verify(authenticationUtil).getCurrentUserId();
    verify(childService).deleteChild(parentId, childId);
  }

  @Test
  @DisplayName("resetPattern() - Should reset child pattern successfully")
  void resetPattern_ValidChildId_ReturnsChild() {
    ChildResponse resetChild = new ChildResponse(
        childId, "Alice", 10, 100, 50, 20, false, parentId);

    when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
    when(childService.resetPattern(parentId, childId)).thenReturn(resetChild);

    ResponseEntity<ChildResponse> response = parentController.resetPattern(childId);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertFalse(response.getBody().hasPattern());

    verify(authenticationUtil).getCurrentUserId();
    verify(childService).resetPattern(parentId, childId);
  }

  // ---------- INVENTORY ENDPOINTS ----------

  @Test
  @DisplayName("getChildInventory() - Should return child inventory")
  void getChildInventory_ValidChildId_ReturnsInventory() {
    InventoryItemResponse item1 = new InventoryItemResponse(
        UUID.randomUUID(),
        UUID.randomUUID(),
        "Toy Car",
        "A cool toy car",
        "🚗",
        25,
        2,
        LocalDateTime.now());

    InventoryItemResponse item2 = new InventoryItemResponse(
        UUID.randomUUID(),
        UUID.randomUUID(),
        "Book",
        "A great book",
        "📖",
        15,
        1,
        LocalDateTime.now());

    List<InventoryItemResponse> inventory = Arrays.asList(item1, item2);

    when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
    when(inventoryService.getChildInventoryForParent(parentId, childId))
        .thenReturn(inventory);

    ResponseEntity<List<InventoryItemResponse>> response = parentController.getChildInventory(childId);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(2, response.getBody().size());
    assertEquals("Toy Car", response.getBody().get(0).name());

    verify(authenticationUtil).getCurrentUserId();
    verify(inventoryService).getChildInventoryForParent(parentId, childId);
  }

  // ---------- MONEY MANAGEMENT ENDPOINTS ----------

  @Test
  @DisplayName("addMoneyToChild() - Should add money successfully")
  void addMoneyToChild_ValidRequest_ReturnsUpdatedChild() {
    AddMoneyRequest request = new AddMoneyRequest(50);
    ChildResponse updatedChild = new ChildResponse(
        childId, "Alice", 10, 100, 50, 70, true, parentId);

    when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
    when(childService.addMoneyToChild(parentId, childId, 50)).thenReturn(updatedChild);

    ResponseEntity<ChildResponse> response = parentController.addMoneyToChild(childId, request);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(70, response.getBody().allowanceMoney());

    verify(authenticationUtil).getCurrentUserId();
    verify(childService).addMoneyToChild(parentId, childId, 50);
  }

  @Test
  @DisplayName("addMoneyToChild() - Should handle zero amount")
  void addMoneyToChild_ZeroAmount_ReturnsUnchangedChild() {
    AddMoneyRequest request = new AddMoneyRequest(0);

    when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
    when(childService.addMoneyToChild(parentId, childId, 0)).thenReturn(childResponse);

    ResponseEntity<ChildResponse> response = parentController.addMoneyToChild(childId, request);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(20, response.getBody().allowanceMoney());

    verify(childService).addMoneyToChild(parentId, childId, 0);
  }

  // ---------- INTEGRATION TESTS ----------

  @Test
  @DisplayName("Integration - Complete child lifecycle")
  void integration_CompleteChildLifecycle_WorksCorrectly() {
    // Setup
    when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);

    // 1. Create child
    CreateChildRequest createRequest = new CreateChildRequest("TestChild", 8);
    ChildResponse newChild = new ChildResponse(
        childId, "TestChild", 8, 0, 0, 0, false, parentId);
    when(childService.createChild(parentId, createRequest)).thenReturn(newChild);

    ResponseEntity<ChildResponse> createResponse = parentController.createChild(createRequest);
    assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

    // 2. Get child
    when(childService.getChildById(parentId, childId)).thenReturn(newChild);
    ResponseEntity<ChildResponse> getResponse = parentController.getChild(childId);
    assertEquals(HttpStatus.OK, getResponse.getStatusCode());

    // 3. Update child
    UpdateChildRequest updateRequest = new UpdateChildRequest("TestChild Updated", 9);
    ChildResponse updatedChild = new ChildResponse(
        childId, "TestChild Updated", 9, 0, 0, 0, false, parentId);
    when(childService.updateChild(parentId, childId, updateRequest)).thenReturn(updatedChild);

    ResponseEntity<ChildResponse> updateResponse = parentController.updateChild(childId, updateRequest);
    assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

    // 4. Delete child
    doNothing().when(childService).deleteChild(parentId, childId);
    ResponseEntity<Void> deleteResponse = parentController.deleteChild(childId);
    assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

    // Verify all operations used correct parent ID
    verify(authenticationUtil, times(4)).getCurrentUserId();
  }

  @Test
  @DisplayName("Integration - All endpoints use authenticated parent ID")
  void integration_AllEndpoints_UseAuthenticatedParentId() {
    when(authenticationUtil.getCurrentUserId()).thenReturn(parentId);
    when(parentService.getParentProfile(parentId)).thenReturn(parentResponse);
    when(childService.getChildrenByParent(parentId)).thenReturn(Arrays.asList());
    when(childService.getChildById(parentId, childId)).thenReturn(childResponse);

    parentController.getMyProfile();
    parentController.getChildren();
    parentController.getChild(childId);

    verify(authenticationUtil, times(3)).getCurrentUserId();
  }
}
