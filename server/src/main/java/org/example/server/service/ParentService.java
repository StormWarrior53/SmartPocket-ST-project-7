package org.example.server.service;

import lombok.RequiredArgsConstructor;
import org.example.server.dto.AuthResponse;
import org.example.server.dto.LoginRequest;
import org.example.server.dto.RegisterParentRequest;
import org.example.server.model.Parent;
import org.example.server.repository.ParentRepository;
import org.example.server.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParentService {

    private final ParentRepository parentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse registerParent(RegisterParentRequest request) {
        if (parentRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        Parent parent = Parent.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        Parent savedParent = parentRepository.save(parent);
        String token = jwtUtil.generateToken(savedParent.getId(), savedParent.getEmail(), "parent");

        return AuthResponse.builder()
                .id(savedParent.getId())
                .email(savedParent.getEmail())
                .firstName(savedParent.getFirstName())
                .lastName(savedParent.getLastName())
                .role("parent")
                .createdAt(savedParent.getCreatedAt())
                .token(token)
                .tokenType("Bearer")
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Parent parent = parentRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), parent.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(parent.getId(), parent.getEmail(), "parent");

        return AuthResponse.builder()
                .id(parent.getId())
                .email(parent.getEmail())
                .firstName(parent.getFirstName())
                .lastName(parent.getLastName())
                .role("parent")
                .createdAt(parent.getCreatedAt())
                .token(token)
                .tokenType("Bearer")
                .build();
    }
}
