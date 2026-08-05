package com.fldb.facilita.auto.api.controller;

import com.fldb.facilita.auto.api.config.security.AuthTokenPrincipal;
import com.fldb.facilita.auto.api.config.security.CustomUserDetails;
import com.fldb.facilita.auto.api.dto.ApiResponseData;
import com.fldb.facilita.auto.api.dto.user.CreateUserRequest;
import com.fldb.facilita.auto.api.dto.user.UserResponse;
import com.fldb.facilita.auto.domain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ResponseEntity<ApiResponseData<UserResponse>> create(
            @Valid @RequestBody CreateUserRequest request,
            @RequestHeader(value = "X-Admin-Api-Key", required = false) String apiKey,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantHeader) {
        log.info("Creating user");

        // Fluxo 1: Autenticação via API Key
        if (apiKey != null) {
            return processApiKeyFlow(request, tenantHeader);
        }

        // Fluxo 2: Autenticação via Token (JWT)
        return processTokenFlow(request);
    }

    // =========================================================================
    // Fluxos de Processamento
    // =========================================================================

    private ResponseEntity<ApiResponseData<UserResponse>> processApiKeyFlow(CreateUserRequest request, String tenantHeader) {
        if (tenantHeader == null || tenantHeader.isBlank()) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, "X-Tenant-ID is required.");
        }

        try {
            UUID tenantId = UUID.fromString(tenantHeader);
            return executeUserCreation(request, tenantId);
        } catch (IllegalArgumentException ex) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid X-Tenant-ID format.");
        }
    }

    private ResponseEntity<ApiResponseData<UserResponse>> processTokenFlow(CreateUserRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID tenantId = extractTenantIdFromPrincipal(auth.getPrincipal());
        if (tenantId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return executeUserCreation(request, tenantId);
    }

    // =========================================================================
    // Métodos Auxiliares
    // =========================================================================

    private UUID extractTenantIdFromPrincipal(Object principal) {
        if (principal instanceof AuthTokenPrincipal atp) {
            return atp.getTenantId();
        }
        if (principal instanceof CustomUserDetails cud) {
            return cud.getTenantId();
        }
        return null;
    }

    private ResponseEntity<ApiResponseData<UserResponse>> executeUserCreation(CreateUserRequest request, UUID tenantId) {
        UserResponse response = userService.create(request, tenantId);
        log.info("User created successfully for tenant: {}", tenantId);

        ApiResponseData<UserResponse> apiResponse = ApiResponseData.<UserResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("User created successfully.")
                .data(response)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    private ResponseEntity<ApiResponseData<UserResponse>> buildErrorResponse(HttpStatus status, String message) {
        ApiResponseData<UserResponse> error = ApiResponseData.<UserResponse>builder()
                .statusCode(status.value())
                .message(message)
                .build();
        return ResponseEntity.status(status).body(error);
    }
}
