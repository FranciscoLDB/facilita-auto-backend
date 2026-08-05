package com.fldb.facilita.auto.api.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final Key signingKey;
    private final long validityInMillis;

    public JwtTokenProvider(
            @Value("${app.security.jwt-secret:very-secret-key}") String secret,
            @Value("${app.security.jwt-expiration-ms:3600000}") long validityInMillis
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.validityInMillis = validityInMillis;
    }

    public String createToken(UUID userId, String email, UUID tenantId, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + validityInMillis);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .claim("tenantId", tenantId.toString())
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> validateToken(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }
}

