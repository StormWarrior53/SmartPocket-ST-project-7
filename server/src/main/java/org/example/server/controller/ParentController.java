package org.example.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.server.dto.LoginRequest;
import org.example.server.dto.ParentResponse;
import org.example.server.dto.RegisterParentRequest;
import org.example.server.service.ParentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;

    @PostMapping("/register")
    public ResponseEntity<ParentResponse> register(@Valid @RequestBody RegisterParentRequest request) {
        ParentResponse response = parentService.registerParent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ParentResponse> login(@Valid @RequestBody LoginRequest request) {
        ParentResponse response = parentService.login(request);
        return ResponseEntity.ok(response);
    }
}
