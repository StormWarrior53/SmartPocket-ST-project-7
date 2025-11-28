package org.example.server.service;

import lombok.RequiredArgsConstructor;
import org.example.server.dto.LoginRequest;
import org.example.server.dto.ParentResponse;
import org.example.server.dto.RegisterParentRequest;
import org.example.server.mapper.ParentMapper;
import org.example.server.model.Parent;
import org.example.server.repository.ParentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParentService {

    private final ParentRepository parentRepository;
    private final PasswordEncoder passwordEncoder;
    private final ParentMapper parentMapper;

    @Transactional
    public ParentResponse registerParent(RegisterParentRequest request) {
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
        return parentMapper.toResponse(savedParent);
    }

    @Transactional(readOnly = true)
    public ParentResponse login(LoginRequest request) {
        Parent parent = parentRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), parent.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return parentMapper.toResponse(parent);
    }
}
