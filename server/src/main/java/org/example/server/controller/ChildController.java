package org.example.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.server.dto.*;
import org.example.server.service.ChildService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/children")
@RequiredArgsConstructor
public class ChildController {

    private final ChildService childService;

    @PostMapping("/check-name")
    public ResponseEntity<CheckNameResponse> checkName(@Valid @RequestBody CheckNameRequest request) {
        CheckNameResponse response = childService.checkNameExists(request.getChildName(), request.getParentName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/setup-pattern")
    public ResponseEntity<ChildAuthResponse> setupPattern(@Valid @RequestBody SetupPatternRequest request) {
        ChildAuthResponse response = childService.setupPattern(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ChildAuthResponse> login(@Valid @RequestBody ChildLoginRequest request) {
        ChildAuthResponse response = childService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ChildListResponse>> getAllChildren() {
        List<ChildListResponse> children = childService.getAllChildren();
        return ResponseEntity.ok(children);
    }

    @GetMapping("/by-parent/{parentId}")
    public ResponseEntity<List<ChildListResponse>> getChildrenByParent(@PathVariable UUID parentId) {
        List<ChildListResponse> children = childService.getChildrenByParentId(parentId);
        return ResponseEntity.ok(children);
    }
}
