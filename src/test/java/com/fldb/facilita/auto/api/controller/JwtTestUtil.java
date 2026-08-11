package com.fldb.facilita.auto.api.controller;

import com.fldb.facilita.auto.api.config.security.JwtTokenProvider;
import com.fldb.facilita.auto.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * Utility para gerar tokens JWT nos testes de integração
 */
@Component
public class JwtTestUtil {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Gera um token JWT válido para testes
     */
    public String generateValidToken(UUID userId, String email, UUID tenantId, String role) {
        return jwtTokenProvider.createToken(userId, email, tenantId, role);
    }

    public String generateValidToken(User user) {
        return jwtTokenProvider.createToken(user.getId(), user.getEmail(), user.getTenantId(), user.getRole().name());
    }

    public String generateAdminTokenForTenant(UUID tenantId) {
        return generateValidToken(
                UUID.fromString("a1111111-1111-1111-1111-111111111111"),
                "admin@tenant.com",
                tenantId,
                "ADMIN"
        );
    }

    /**
     * Gera um token para um usuário finance do tenant 1
     */
    public String generateFinanceToken() {
        return generateValidToken(
                UUID.fromString("a3333333-3333-3333-3333-333333333333"),
                "finance@tenant1.com",
                UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"),
                "FINANCE"
        );
    }


    /**
     * Gera um token expirado (data past)
     * Para simular um token expirado, criamos com expiração negativa
     * Nota: JwtTokenProvider não expõe criar tokens com expiração customizada,
     * então esta é uma simulação que o teste pode fazer manualmente
     */
    public String generateExpiredToken() {
        // Retorna um token inválido para simular expiração
        // Na prática, o teste pode usar um token antigo ou manipular o método
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjB9.invalidtoken";
    }
}

