package org.example.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.server.dto.*;
import org.example.server.service.ChildService;
import org.example.server.service.ParentService;
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

    // Child management endpoints

    @PostMapping("/{parentId}/children")
    public ResponseEntity<ChildResponse> createChild(
            @PathVariable UUID parentId,
            @Valid @RequestBody CreateChildRequest request) {
        ChildResponse response = childService.createChild(parentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{parentId}/children")
    public ResponseEntity<List<ChildResponse>> getChildren(@PathVariable UUID parentId) {
        List<ChildResponse> children = childService.getChildrenByParent(parentId);
        return ResponseEntity.ok(children);
    }

    @GetMapping("/{parentId}/children/{childId}")
    public ResponseEntity<ChildResponse> getChild(
            @PathVariable UUID parentId,
            @PathVariable UUID childId) {
        ChildResponse child = childService.getChildById(parentId, childId);
        return ResponseEntity.ok(child);
    }

    @PutMapping("/{parentId}/children/{childId}")
    public ResponseEntity<ChildResponse> updateChild(
            @PathVariable UUID parentId,
            @PathVariable UUID childId,
            @Valid @RequestBody UpdateChildRequest request) {
        ChildResponse response = childService.updateChild(parentId, childId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{parentId}/children/{childId}")
    public ResponseEntity<Void> deleteChild(
            @PathVariable UUID parentId,
            @PathVariable UUID childId) {
        childService.deleteChild(parentId, childId);
        return ResponseEntity.noContent().build();
    }
}
