package org.example.server.service;

import org.example.server.dto.ChildResponse;
import org.example.server.dto.CreateChildRequest;
import org.example.server.dto.UpdateChildRequest;
import org.example.server.exception.AccessDeniedException;
import org.example.server.exception.ResourceNotFoundException;
import org.example.server.model.Child;
import org.example.server.model.Parent;
import org.example.server.repository.ChildRepository;
import org.example.server.repository.ParentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ChildService.
 *
 * Covers:
 * - CRUD operations for children
 * - Parent authorization checks
 * - Money management
 * - Error scenarios
 */
@DisplayName("ChildService - Unit Tests")
class ChildServiceTest {

    @Mock
    private ChildRepository childRepository;

    @Mock
    private ParentRepository parentRepository;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private ChildService childService;

    private Parent parent;
    private Child child;
    private UUID parentId;
    private UUID childId;
    private UUID otherParentId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        parentId = UUID.randomUUID();
        childId = UUID.randomUUID();
        otherParentId = UUID.randomUUID();

        parent = Parent.builder()
                .id(parentId)
                .email("parent@example.com")
                .firstName("John")
                .lastName("Doe")
                .passwordHash("hashedPassword")
                .build();

        child = Child.builder()
                .id(childId)
                .name("Alice")
                .age(10)
                .xp(100)
                .pocketMoney(50)
                .allowanceMoney(20)
                .pinHash(null)
                .parent(parent)
                .build();
    }

    // ---------- CREATE CHILD ----------

    @Test
    @DisplayName("createChild() - Should create child successfully")
    void createChild_ValidRequest_CreatesChild() {
        CreateChildRequest request = new CreateChildRequest("Bob", 8);

        when(parentRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(childRepository.save(any(Child.class))).thenAnswer(i -> {
            Child savedChild = i.getArgument(0);
            savedChild.setId(UUID.randomUUID());
            return savedChild;
        });

        ChildResponse response = childService.createChild(parentId, request);

        assertNotNull(response);
        assertEquals("Bob", response.name());
        assertEquals(8, response.age());
        assertEquals(0, response.xp());
        assertEquals(0, response.pocketMoney());
        assertEquals(0, response.allowanceMoney());
        assertFalse(response.hasPattern());

        verify(parentRepository).findById(parentId);
        verify(childRepository).save(any(Child.class));
    }

    @Test
    @DisplayName("createChild() - Should throw exception when parent not found")
    void createChild_ParentNotFound_ThrowsException() {
        CreateChildRequest request = new CreateChildRequest("Bob", 8);

        when(parentRepository.findById(parentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> childService.createChild(parentId, request));

        verify(childRepository, never()).save(any(Child.class));
    }

    // ---------- GET CHILD BY ID ----------

    @Test
    @DisplayName("getChildById() - Should return child when parent matches")
    void getChildById_ValidParent_ReturnsChild() {
        when(childRepository.findById(childId)).thenReturn(Optional.of(child));

        ChildResponse response = childService.getChildById(parentId, childId);

        assertNotNull(response);
        assertEquals(childId, response.id());
        assertEquals("Alice", response.name());
        assertEquals(10, response.age());

        verify(childRepository).findById(childId);
    }

    @Test
    @DisplayName("getChildById() - Should throw exception when child not found")
    void getChildById_ChildNotFound_ThrowsException() {
        when(childRepository.findById(childId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> childService.getChildById(parentId, childId));
    }

    @Test
    @DisplayName("getChildById() - Should throw exception when parent doesn't match")
    void getChildById_WrongParent_ThrowsAccessDeniedException() {
        when(childRepository.findById(childId)).thenReturn(Optional.of(child));

        assertThrows(AccessDeniedException.class,
                () -> childService.getChildById(otherParentId, childId));
    }

    // ---------- GET CHILDREN BY PARENT ----------

    @Test
    @DisplayName("getChildrenByParent() - Should return list of children")
    void getChildrenByParent_ReturnsChildrenList() {
        Child child2 = Child.builder()
                .id(UUID.randomUUID())
                .name("Charlie")
                .age(12)
                .xp(200)
                .pocketMoney(75)
                .allowanceMoney(30)
                .parent(parent)
                .build();

        when(childRepository.findByParentId(parentId))
                .thenReturn(Arrays.asList(child, child2));

        List<ChildResponse> children = childService.getChildrenByParent(parentId);

        assertNotNull(children);
        assertEquals(2, children.size());
        assertEquals("Alice", children.get(0).name());
        assertEquals("Charlie", children.get(1).name());

        verify(childRepository).findByParentId(parentId);
    }

    @Test
    @DisplayName("getChildrenByParent() - Should return empty list when no children")
    void getChildrenByParent_NoChildren_ReturnsEmptyList() {
        when(childRepository.findByParentId(parentId)).thenReturn(Arrays.asList());

        List<ChildResponse> children = childService.getChildrenByParent(parentId);

        assertNotNull(children);
        assertTrue(children.isEmpty());
    }

    // ---------- UPDATE CHILD ----------

    @Test
    @DisplayName("updateChild() - Should update child successfully")
    void updateChild_ValidRequest_UpdatesChild() {
        UpdateChildRequest request = new UpdateChildRequest("Alice Updated", 11);

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(childRepository.save(any(Child.class))).thenAnswer(i -> i.getArgument(0));

        ChildResponse response = childService.updateChild(parentId, childId, request);

        assertNotNull(response);
        assertEquals("Alice Updated", response.name());
        assertEquals(11, response.age());

        verify(childRepository).findById(childId);
        verify(childRepository).save(child);
    }

    @Test
    @DisplayName("updateChild() - Should throw exception when parent doesn't match")
    void updateChild_WrongParent_ThrowsAccessDeniedException() {
        UpdateChildRequest request = new UpdateChildRequest("Alice Updated", 11);

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));

        assertThrows(AccessDeniedException.class,
                () -> childService.updateChild(otherParentId, childId, request));

        verify(childRepository, never()).save(any(Child.class));
    }

    // ---------- DELETE CHILD ----------

    @Test
    @DisplayName("deleteChild() - Should delete child successfully")
    void deleteChild_ValidRequest_DeletesChild() {
        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        doNothing().when(childRepository).delete(child);

        childService.deleteChild(parentId, childId);

        verify(childRepository).findById(childId);
        verify(childRepository).delete(child);
    }

    @Test
    @DisplayName("deleteChild() - Should throw exception when parent doesn't match")
    void deleteChild_WrongParent_ThrowsAccessDeniedException() {
        when(childRepository.findById(childId)).thenReturn(Optional.of(child));

        assertThrows(AccessDeniedException.class,
                () -> childService.deleteChild(otherParentId, childId));

        verify(childRepository, never()).delete(any(Child.class));
    }

    // ---------- ADD MONEY TO CHILD ----------

    @Test
    @DisplayName("addMoneyToChild() - Should add money successfully")
    void addMoneyToChild_ValidAmount_AddsMoney() {
        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(childRepository.save(any(Child.class))).thenAnswer(i -> i.getArgument(0));

        ChildResponse response = childService.addMoneyToChild(parentId, childId, 50);

        assertNotNull(response);
        assertEquals(70, response.allowanceMoney()); // 20 + 50

        verify(childRepository).findById(childId);
        verify(childRepository).save(child);
    }

    @Test
    @DisplayName("addMoneyToChild() - Should throw exception when parent doesn't match")
    void addMoneyToChild_WrongParent_ThrowsAccessDeniedException() {
        when(childRepository.findById(childId)).thenReturn(Optional.of(child));

        assertThrows(AccessDeniedException.class,
                () -> childService.addMoneyToChild(otherParentId, childId, 50));

        verify(childRepository, never()).save(any(Child.class));
    }

    // ---------- RESET PATTERN ----------

    @Test
    @DisplayName("resetPattern() - Should reset pattern successfully")
    void resetPattern_ValidRequest_ResetsPattern() {
        child.setPinHash("hashedPattern");

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(childRepository.save(any(Child.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(tokenBlacklistService).blacklistAllUserTokens(any(), any(), any());

        ChildResponse response = childService.resetPattern(parentId, childId);

        assertNotNull(response);
        assertFalse(response.hasPattern());
        assertNull(child.getPinHash());

        verify(childRepository).findById(childId);
        verify(childRepository).save(child);
        verify(tokenBlacklistService).blacklistAllUserTokens(eq(childId), any(), any());
    }

    @Test
    @DisplayName("resetPattern() - Should throw exception when parent doesn't match")
    void resetPattern_WrongParent_ThrowsAccessDeniedException() {
        when(childRepository.findById(childId)).thenReturn(Optional.of(child));

        assertThrows(AccessDeniedException.class,
                () -> childService.resetPattern(otherParentId, childId));

        verify(childRepository, never()).save(any(Child.class));
        verify(tokenBlacklistService, never()).blacklistAllUserTokens(any(), any(), any());
    }

    // ---------- PATCH METHODS ----------

    @Test
    @DisplayName("patchPocketMoneyMe() - Should update pocket money")
    void patchPocketMoneyMe_ValidAmount_UpdatesMoney() {
        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(childRepository.save(any(Child.class))).thenAnswer(i -> i.getArgument(0));

        ChildResponse response = childService.patchPocketMoneyMe(childId, 25);

        assertNotNull(response);
        assertEquals(75, response.pocketMoney()); // 50 + 25

        verify(childRepository).save(child);
    }

    @Test
    @DisplayName("patchPocketMoneyMe() - Should throw exception for negative amount")
    void patchPocketMoneyMe_NegativeAmount_ThrowsException() {
        when(childRepository.findById(childId)).thenReturn(Optional.of(child));

        assertThrows(IllegalArgumentException.class,
                () -> childService.patchPocketMoneyMe(childId, -10));

        verify(childRepository, never()).save(any(Child.class));
    }

    @Test
    @DisplayName("patchXpMe() - Should update XP")
    void patchXpMe_ValidAmount_UpdatesXp() {
        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(childRepository.save(any(Child.class))).thenAnswer(i -> i.getArgument(0));

        ChildResponse response = childService.patchXpMe(childId, 50);

        assertNotNull(response);
        assertEquals(150, response.xp()); // 100 + 50

        verify(childRepository).save(child);
    }

    @Test
    @DisplayName("patchXpMe() - Should throw exception for negative amount")
    void patchXpMe_NegativeAmount_ThrowsException() {
        when(childRepository.findById(childId)).thenReturn(Optional.of(child));

        assertThrows(IllegalArgumentException.class,
                () -> childService.patchXpMe(childId, -10));

        verify(childRepository, never()).save(any(Child.class));
    }
}
