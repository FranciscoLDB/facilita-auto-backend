package com.fldb.facilita.auto.api.config.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class AuthTokenPrincipal {
    private final UUID id;
    private final String email;
    private final UUID tenantId;
    private final String role;

    public AuthTokenPrincipal(UUID id, String email, UUID tenantId, String role) {
        this.id = id;
        this.email = email;
        this.tenantId = tenantId;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getRole() {
        return role;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }
}

