package com.fldb.facilita.auto.api.controller;

import com.fldb.facilita.auto.api.config.security.JwtTokenProvider;
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

    /**
     * Gera um token para um usuário admin do tenant 1
     */
    public String generateAdminToken() {
        return generateValidToken(
                UUID.fromString("a1111111-1111-1111-1111-111111111111"),
                "admin@tenant1.com",
                UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"),
                "ADMIN"
        );
    }

    /**
     * Gera um token para um usuário operator do tenant 1
     */
    public String generateOperatorToken() {
        return generateValidToken(
                UUID.fromString("a2222222-2222-2222-2222-222222222222"),
                "operator@tenant1.com",
                UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"),
                "OPERATOR"
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
     * Gera um token para um usuário admin do tenant 2
     */
    public String generateAdminTokenTenant2() {
        return generateValidToken(
                UUID.fromString("b1111111-1111-1111-1111-111111111111"),
                "admin@tenant2.com",
                UUID.fromString("b1ffcd00-0d1c-5fa9-cc7e-7cc0ce491b22"),
                "ADMIN"
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

