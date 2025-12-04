package org.example.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.server.dto.*;
import org.example.server.service.ChildService;
import org.example.server.service.ParentService;
import org.example.server.util.AuthenticationUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;
    private final ChildService childService;
    private final AuthenticationUtil authenticationUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterParentRequest request) {
        AuthResponse response = parentService.registerParent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = parentService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ParentResponse> getMyProfile() {
        UUID parentId = authenticationUtil.getCurrentUserId();
        ParentResponse response = parentService.getParentProfile(parentId);
        return ResponseEntity.ok(response);
    }

    // Child management endpoints - all use authenticated parent ID

    @PostMapping("/me/children")
    public ResponseEntity<ChildResponse> createChild(@Valid @RequestBody CreateChildRequest request) {
        UUID parentId = authenticationUtil.getCurrentUserId();
        ChildResponse response = childService.createChild(parentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me/children")
    public ResponseEntity<List<ChildResponse>> getChildren() {
        UUID parentId = authenticationUtil.getCurrentUserId();
        List<ChildResponse> children = childService.getChildrenByParent(parentId);
        return ResponseEntity.ok(children);
    }

    @GetMapping("/me/children/{childId}")
    public ResponseEntity<ChildResponse> getChild(@PathVariable UUID childId) {
        UUID parentId = authenticationUtil.getCurrentUserId();
        ChildResponse child = childService.getChildById(parentId, childId);
        return ResponseEntity.ok(child);
    }

    @PutMapping("/me/children/{childId}")
    public ResponseEntity<ChildResponse> updateChild(
            @PathVariable UUID childId,
            @Valid @RequestBody UpdateChildRequest request) {
        UUID parentId = authenticationUtil.getCurrentUserId();
        ChildResponse response = childService.updateChild(parentId, childId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me/children/{childId}")
    public ResponseEntity<Void> deleteChild(@PathVariable UUID childId) {
        UUID parentId = authenticationUtil.getCurrentUserId();
        childService.deleteChild(parentId, childId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/children/{childId}/reset-pattern")
    public ResponseEntity<ChildResponse> resetPattern(@PathVariable UUID childId) {
        UUID parentId = authenticationUtil.getCurrentUserId();
        ChildResponse response = childService.resetPattern(parentId, childId);
        return ResponseEntity.ok(response);
    }
}
