package com.fldb.facilita.auto.api.config.security.filter;

import com.fldb.facilita.auto.api.config.security.AuthTokenPrincipal;
import com.fldb.facilita.auto.api.exception.ErrorCode;
import com.fldb.facilita.auto.api.exception.model.ApiResponseError;
import com.fldb.facilita.auto.api.exception.model.GeneralErrorItem;
import com.fldb.facilita.auto.domain.repository.TenantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class AdminApiKeyFilter extends OncePerRequestFilter {

    @Autowired
    private TenantRepository tenantRepository;

    private static final String HEADER_NAME = "X-Admin-Api-Key";
    private static final String HEADER_TENANT_ID = "X-Tenant-ID";

    @Value("${app.security.admin-api-key}")
    private String adminApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestApiKey = request.getHeader(HEADER_NAME);

        if (requestApiKey != null && requestApiKey.equals(adminApiKey)) {

            // Read X-Tenant-ID header if present
            String tenantHeader = request.getHeader(HEADER_TENANT_ID);
            UUID tenantId = null;

            if (tenantHeader != null && !tenantHeader.isBlank()) {
                try {
                    tenantId = UUID.fromString(tenantHeader);
                    if (!tenantRepository.existsById(tenantId)) {
                        setResponse(response, "Tenant não encontrado.");
                        return;
                    }
                } catch (Exception e) {
                    setResponse(response, e.getMessage());
                    return;
                }
            }

            // Create AuthTokenPrincipal containing the target tenantId from header
            AuthTokenPrincipal principal = new AuthTokenPrincipal(
                    UUID.randomUUID(),
                    "ADMIN_KEY_USER",
                    tenantId,
                    "MASTER"
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            principal.getAuthorities()
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private static void setResponse(HttpServletResponse response, String detailedMessage) throws IOException {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponseError errorResponse = ApiResponseError.builder()
                .statusCode(status.value())
                .statusMessage(status.name())
                .message("ID do tenant inválido.")
                .detailedMessage(detailedMessage)
                .timestamp(OffsetDateTime.now()).errors(List.of(
                        GeneralErrorItem.builder()
                                .code(ErrorCode.TENANT_INVALID_ID)
                                .message(ErrorCode.TENANT_INVALID_ID.getDefaultMessage())
                                .build()
                ))
                .build();

        new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
    }
}
