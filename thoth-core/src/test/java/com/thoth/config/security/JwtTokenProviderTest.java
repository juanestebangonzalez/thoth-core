package com.thoth.config.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for SEC-004: JwtTokenProvider used to fall back to a
 * hardcoded, publicly-known secret ("thoth-core-secret-key-must-be-at-least-
 * 256-bits-long-for-hs256") whenever jwt.secret was not configured, letting
 * anyone forge valid admin tokens offline.
 */
class JwtTokenProviderTest {

    private static final String OLD_PUBLIC_DEFAULT_SECRET =
        "thoth-core-secret-key-must-be-at-least-256-bits-long-for-hs256";

    @Test
    void validateToken_rejectsTokenSignedWithFormerHardcodedDefaultSecret() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", "a-completely-different-real-secret-configured-per-environment");
        ReflectionTestUtils.setField(provider, "jwtExpiration", 86400000L);

        String forgedToken = forgeTokenWithSecret(OLD_PUBLIC_DEFAULT_SECRET, "attacker", "ADMIN");

        assertFalse(provider.validateToken(forgedToken));
    }

    @Test
    void validateToken_acceptsTokenSignedWithConfiguredSecret() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", "a-completely-different-real-secret-configured-per-environment");
        ReflectionTestUtils.setField(provider, "jwtExpiration", 86400000L);

        String token = provider.generateToken("someuser", "USER");

        assertTrue(provider.validateToken(token));
    }

    private String forgeTokenWithSecret(String secret, String username, String role) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        return Jwts.builder()
            .subject(username)
            .claim("role", role)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + 86400000L))
            .signWith(key)
            .compact();
    }
}
