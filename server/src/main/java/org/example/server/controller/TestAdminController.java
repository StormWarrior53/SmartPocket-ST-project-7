package org.example.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.server.dto.AuthResponse;
import org.example.server.dto.RegisterParentRequest;
import org.example.server.model.Parent;
import org.example.server.repository.ParentRepository;
import org.example.server.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * TEST ONLY - This controller is for creating admin users during testing.
 * REMOVE THIS FILE IN PRODUCTION!
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestAdminController {

    private final ParentRepository parentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/create-admin")
    public ResponseEntity<AuthResponse> createTestAdmin(@RequestBody RegisterParentRequest request) {
        log.warn("TEST ONLY - Creating admin user: {}", request.email());

        if (parentRepository.existsByEmail(request.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Parent admin = Parent.builder()
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        Parent savedAdmin = parentRepository.save(admin);

        // Generate token with ADMIN role
        String token = jwtUtil.generateToken(savedAdmin.getId(), savedAdmin.getEmail(), "admin");
        Date expirationDate = jwtUtil.getExpirationDate(token);
        Long expiresAt = expirationDate.getTime();

        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(
                savedAdmin.getId(),
                savedAdmin.getEmail(),
                savedAdmin.getFirstName(),
                savedAdmin.getLastName(),
                "admin",
                savedAdmin.getCreatedAt(),
                token,
                expiresAt
        ));
    }
}
