package com.fldb.facilita.auto.api.controller;

import com.fldb.facilita.auto.api.config.security.AuthTokenPrincipal;
import com.fldb.facilita.auto.api.config.security.CustomUserDetails;
import com.fldb.facilita.auto.api.dto.ApiResponseData;
import com.fldb.facilita.auto.api.dto.user.CreateUserRequest;
import com.fldb.facilita.auto.api.dto.user.UserResponse;
import com.fldb.facilita.auto.api.exception.BusinessException;
import com.fldb.facilita.auto.domain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponseData<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        log.info("Creating user");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID tenantId = extractTenantIdFromPrincipal(auth.getPrincipal());

        UserResponse response = userService.create(request, tenantId);
        log.info("User created successfully for tenant: {}", tenantId);

        ApiResponseData<UserResponse> apiResponse = ApiResponseData.<UserResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("User created successfully.")
                .data(response)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponseData<Page<UserResponse>>> findAll(Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Page<UserResponse> users = userService.findAll(pageable);

        ApiResponseData<Page<UserResponse>> response = ApiResponseData.<Page<UserResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Users retrieved successfully.")
                .data(users)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseData<Void>> delete(@PathVariable UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Must not delete own user
        if (id.equals(extractUserIdFromPrincipal(auth.getPrincipal()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // Métodos Auxiliares
    // =========================================================================

    private UUID extractUserIdFromPrincipal(Object principal) {
        if (principal instanceof AuthTokenPrincipal atp) {
            return atp.getId();
        }
        if (principal instanceof CustomUserDetails cud) {
            return cud.getId();
        }
        return null;
    }

    private UUID extractTenantIdFromPrincipal(Object principal) {
        UUID tenantId = null;

        if (principal instanceof AuthTokenPrincipal atp) {
            tenantId = atp.getTenantId();
        }
        if (principal instanceof CustomUserDetails cud) {
            tenantId = cud.getTenantId();
        }

        if (tenantId == null) {
            throw new BusinessException("Tenant não encontrado.");
        }

        return tenantId;
    }
}
