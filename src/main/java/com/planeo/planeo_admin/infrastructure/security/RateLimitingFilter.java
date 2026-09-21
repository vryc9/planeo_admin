package com.planeo.planeo_admin.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lightweight per-IP fixed-window rate limiter for the public,
 * token-guessable endpoints (invitation validation and registration).
 *
 * The invitation token itself has 256 bits of entropy, so brute-forcing it is
 * not practically feasible; this filter is defense in depth against abuse
 * and enumeration, not the primary control.
 *
 * This is in-memory and per-instance: behind multiple replicas it only
 * limits per-instance traffic. A shared store (e.g. Redis) would be needed
 * for a hard guarantee across a horizontally scaled deployment - out of
 * scope for this change.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_WINDOW = 20;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final ConcurrentHashMap<String, RequestWindow> windowsByClient = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !isRateLimited(request.getMethod(), request.getRequestURI());
    }

    private boolean isRateLimited(String method, String path) {
        boolean isRegister = "POST".equals(method) && "/register".equals(path);
        boolean isInvitationPreview = "GET".equals(method) && path.startsWith("/invitations/validate/");
        return isRegister || isInvitationPreview;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String clientKey = clientKey(request);
        RequestWindow window = windowsByClient.computeIfAbsent(clientKey, key -> new RequestWindow());

        if (!window.tryConsume()) {
            RestAuthenticationEntryPoint.writeError(
                    response,
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many requests, please try again later.",
                    request.getRequestURI()
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String clientKey(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private static final class RequestWindow {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile Instant windowStart = Instant.now();

        synchronized boolean tryConsume() {
            Instant now = Instant.now();
            if (Duration.between(windowStart, now).compareTo(WINDOW) > 0) {
                windowStart = now;
                count.set(0);
            }
            return count.incrementAndGet() <= MAX_REQUESTS_PER_WINDOW;
        }
    }
}
