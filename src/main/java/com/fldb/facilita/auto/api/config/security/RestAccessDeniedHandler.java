package com.fldb.facilita.auto.api.config.security;

import com.fldb.facilita.auto.api.exception.model.ApiResponseError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.OffsetDateTime;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        ApiResponseError errorResponse = ApiResponseError.builder()
                .statusCode(HttpServletResponse.SC_FORBIDDEN)
                .statusMessage("FORBIDDEN")
                .message("Access is denied")
                .detailedMessage(accessDeniedException.getMessage())
                .timestamp(OffsetDateTime.now())
                .build();

        new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
    }
}

