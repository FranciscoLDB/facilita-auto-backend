package com.fldb.facilita.auto.domain.service;

import com.fldb.facilita.auto.api.config.security.AuthTokenPrincipal;
import com.fldb.facilita.auto.api.config.security.CustomUserDetails;
import com.fldb.facilita.auto.api.exception.BusinessException;

import java.util.UUID;

public class PrincipalUtil {

    public static UUID extractUserIdFromPrincipal(Object principal) {
        if (principal instanceof AuthTokenPrincipal atp) {
            return atp.getId();
        }
        if (principal instanceof CustomUserDetails cud) {
            return cud.getId();
        }
        return null;
    }

    public static UUID extractTenantIdFromPrincipal(Object principal) {
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
