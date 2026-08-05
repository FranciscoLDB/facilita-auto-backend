package com.fldb.facilita.auto.api.controller;

import com.fldb.facilita.auto.api.config.security.CustomUserDetails;
import com.fldb.facilita.auto.api.config.security.JwtTokenProvider;
import com.fldb.facilita.auto.api.dto.ApiResponseData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<ApiResponseData<String>> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        CustomUserDetails details = (CustomUserDetails) authentication.getPrincipal();

        assert details != null;
        String token = jwtTokenProvider.createToken(details.getId(), details.getUsername(), details.getTenantId(), details.getAuthorities().stream().findFirst().get().getAuthority());

        ApiResponseData<String> apiResponse = ApiResponseData.<String>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Autenticado com sucesso.")
                .data(token)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    public static record LoginRequest(String email, String password) {}
}


