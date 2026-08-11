package com.fldb.facilita.auto.util;

import com.fldb.facilita.auto.api.config.security.AuthTokenPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public final class TenantTestUtils {

    // Construtor privado para evitar instanciação da classe utilitária
    private TenantTestUtils() {
        throw new UnsupportedOperationException("Classe utilitária não deve ser instanciada.");
    }

    /**
     * Executa um Runnable dentro do contexto de segurança de um Tenant específico.
     */
    public static void runInTenantContext(UUID tenantId, Runnable action) {
        runInTenantContext(tenantId, () -> {
            action.run();
            return null;
        });
    }

    /**
     * Executa um Supplier dentro do contexto de segurança de um Tenant específico e retorna o resultado.
     */
    public static <T> T runInTenantContext(UUID tenantId, Supplier<T> supplier) {
        var previousAuth = SecurityContextHolder.getContext().getAuthentication();
        try {
            AuthTokenPrincipal principal = new AuthTokenPrincipal(
                    UUID.randomUUID(),
                    "ADMIN_KEY_USER",
                    tenantId,
                    "MASTER"
            );
            var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);

            return supplier.get();
        } finally {
            SecurityContextHolder.getContext().setAuthentication(previousAuth);
        }
    }
}