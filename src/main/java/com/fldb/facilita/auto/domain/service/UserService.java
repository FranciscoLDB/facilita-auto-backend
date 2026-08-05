package com.fldb.facilita.auto.domain.service;

import com.fldb.facilita.auto.api.dto.user.CreateUserRequest;
import com.fldb.facilita.auto.api.dto.user.UserResponse;
import com.fldb.facilita.auto.api.exception.BusinessException;
import com.fldb.facilita.auto.api.exception.ResourceNotFoundException;
import com.fldb.facilita.auto.domain.entity.Tenant;
import com.fldb.facilita.auto.domain.entity.User;
import com.fldb.facilita.auto.domain.repository.TenantRepository;
import com.fldb.facilita.auto.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse create(CreateUserRequest request, UUID tenantId) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Já existe um usuário cadastrado com este e-mail.");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant não encontrado."));

        User user = User.builder()
                .tenant(tenant)
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isActive(true)
                .build();

        userRepository.save(user);

        return UserResponse.builder()
                .id(user.getId())
                .tenantId(user.getTenant().getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}