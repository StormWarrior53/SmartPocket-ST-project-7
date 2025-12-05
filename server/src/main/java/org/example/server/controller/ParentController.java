package org.example.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.server.dto.AuthResponse;
import org.example.server.dto.LoginRequest;
import org.example.server.dto.RegisterParentRequest;
import org.example.server.service.ParentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
public class ParentController {
    private static final Logger log = LoggerFactory.getLogger(ParentController.class);

    private final ParentService parentService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterParentRequest request) {
        log.info("Received parent registration request for email={}", request.getEmail());
        AuthResponse response = parentService.registerParent(request);
        log.info("Parent successfully registered with id={}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email={}", request.getEmail());
        AuthResponse response = parentService.login(request);
        log.info("Login successful for parentId={}", response.getId());
        return ResponseEntity.ok(response);
    }
}
