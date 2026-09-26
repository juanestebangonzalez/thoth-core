package com.thoth.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SEC-008 fix: /api/v1/auth/** had no protection against brute force /
 * credential stuffing. This applies a simple fixed-window rate limit, keyed
 * by client IP + path, to the auth endpoints most exposed to abuse.
 *
 * DT-10: Mejorado con headers estándar (X-RateLimit-*, Retry-After)
 * y limpieza periódica de ventanas expiradas.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Set<String> PROTECTED_PATHS = Set.of(
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/auth/change-password",
        "/api/v1/auth/request-password-reset"
    );

    private final int maxAttempts;
    private final long windowMillis;
    private final ConcurrentHashMap<String, Window> buckets = new ConcurrentHashMap<>();

    public RateLimitingFilter(
            @Value("${security.rate-limit.max-attempts:15}") int maxAttempts,
            @Value("${security.rate-limit.window-ms:60000}") long windowMillis) {
        this.maxAttempts = maxAttempts;
        this.windowMillis = windowMillis;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (!PROTECTED_PATHS.contains(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        String key = clientIp(request) + ":" + request.getRequestURI();
        long now = System.currentTimeMillis();

        Window window = buckets.compute(key, (k, existing) -> {
            if (existing == null || (now - existing.windowStart) > windowMillis) {
                return new Window(now, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });

        int attempts = window.count.get();

        // Periodic cleanup of expired windows to prevent memory leak
        if (attempts == 1) {
            buckets.entrySet().removeIf(e -> (now - e.getValue().windowStart) > windowMillis * 2);
        }

        // DT-10: Headers estándar de rate limiting
        int remaining = Math.max(0, maxAttempts - attempts);
        long windowResetAt = window.windowStart + windowMillis;
        long retryAfterSeconds = Math.max(1, (windowResetAt - now) / 1000);

        response.setIntHeader("X-RateLimit-Limit", maxAttempts);
        response.setIntHeader("X-RateLimit-Remaining", remaining);
        response.setHeader("X-RateLimit-Reset", String.valueOf(windowResetAt / 1000));

        if (attempts > maxAttempts) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.getWriter().write(
                "{\"status\":429,\"error\":\"TOO_MANY_REQUESTS\",\"message\":\"Demasiados intentos. Intente de nuevo en "
                + retryAfterSeconds + " segundos.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private static final class Window {
        final long windowStart;
        final AtomicInteger count;

        Window(long windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
