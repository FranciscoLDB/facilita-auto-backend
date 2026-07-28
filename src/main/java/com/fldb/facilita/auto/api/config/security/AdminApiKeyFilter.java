package com.fldb.facilita.auto.api.config.security;

import com.fldb.facilita.auto.api.exception.model.ApiResponseError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.OffsetDateTime;

@Component
public class AdminApiKeyFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "X-Admin-Api-Key";

    @Value("${app.security.admin-api-key}")
    private String adminApiKey;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/v1/tenants");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestApiKey = request.getHeader(HEADER_NAME);

        if (requestApiKey == null || !requestApiKey.equals(adminApiKey)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ApiResponseError errorResponse = ApiResponseError.builder()
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .statusMessage(HttpStatus.UNAUTHORIZED.name())
                    .message("Not authorized")
                    .detailedMessage("Secret not valid")
                    .timestamp(OffsetDateTime.now())
                    .build();

            new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
