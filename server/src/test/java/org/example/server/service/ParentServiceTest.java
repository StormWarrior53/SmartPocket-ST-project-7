package org.example.server.service;

import org.example.server.dto.parent.ParentProfileDTO;
import org.example.server.dto.parent.UpdateParentProfileDTO;
import org.example.server.dto.child.AddMoneyRequest;
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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ParentService - Unit Tests")
class ParentServiceTest {

    @Mock
    private ParentRepository parentRepository;

    @Mock
    private ChildRepository childRepository;

    @InjectMocks
    private ParentService parentService;

    private Parent parent;
    private UUID parentId;
    private UUID childId;
    private Child child;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        parentId = UUID.randomUUID();
        childId = UUID.randomUUID();

        parent = new Parent(parentId, "parent1", "parent1@example.com", "Password123");
        child = new Child(childId, "Johnny", 10, 0, 0, 0, false, parent);
    }

    @Test
    @DisplayName("getParentProfile() - Should return profile DTO")
    void getParentProfile_ShouldReturnProfileDTO() {
        when(parentRepository.findById(parentId)).thenReturn(Optional.of(parent));

        ParentProfileDTO profile = parentService.getParentProfile(parentId);

        assertNotNull(profile);
        assertEquals(parent.getUsername(), profile.username());
        assertEquals(parent.getEmail(), profile.email());
        verify(parentRepository, times(1)).findById(parentId);
    }

    @Test
    @DisplayName("updateParentProfile() - Should update parent and return updated DTO")
    void updateParentProfile_ShouldUpdateAndReturnDTO() {
        when(parentRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(parentRepository.save(any(Parent.class))).thenAnswer(i -> i.getArgument(0));

        UpdateParentProfileDTO request = new UpdateParentProfileDTO("newParent", "newemail@example.com");
        ParentProfileDTO updatedProfile = parentService.updateParentProfile(parentId, request);

        assertNotNull(updatedProfile);
        assertEquals("newParent", updatedProfile.username());
        assertEquals("newemail@example.com", updatedProfile.email());

        verify(parentRepository).findById(parentId);
        verify(parentRepository).save(any(Parent.class));
    }

    @Test
    @DisplayName("addMoneyToChild() - Should increase child's pocket money")
    void addMoneyToChild_ShouldIncreaseMoney() {
        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(childRepository.save(any(Child.class))).thenAnswer(i -> i.getArgument(0));

        AddMoneyRequest request = new AddMoneyRequest(50);
        var updatedChild = parentService.addMoneyToChild(parentId, childId, request.amount());

        assertEquals(50, updatedChild.pocketMoney());
        verify(childRepository).findById(childId);
        verify(childRepository).save(any(Child.class));
    }

    @Test
    @DisplayName("addMoneyToChild() - Should throw exception if child not found")
    void addMoneyToChild_ChildNotFound_ShouldThrow() {
        when(childRepository.findById(childId)).thenReturn(Optional.empty());

        AddMoneyRequest request = new AddMoneyRequest(50);

        assertThrows(RuntimeException.class,
                () -> parentService.addMoneyToChild(parentId, childId, request.amount()));

        verify(childRepository).findById(childId);
        verify(childRepository, never()).save(any(Child.class));
    }
}