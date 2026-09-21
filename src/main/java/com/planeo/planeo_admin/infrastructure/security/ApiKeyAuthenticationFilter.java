package com.planeo.planeo_admin.infrastructure.security;

import com.planeo.planeo_admin.infrastructure.config.ApiKeyProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * Authenticates admin-only calls (invitation creation/revocation) using a
 * shared secret passed in the X-Api-Key header, compared in constant time to
 * avoid leaking the key through response-time side channels.
 *
 * This does not attempt to authenticate any other endpoint: absence or
 * mismatch of the header simply leaves the request anonymous, and the
 * authorization rules in SecurityConfig decide whether that is enough.
 */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-Api-Key";

    private final ApiKeyProperties apiKeyProperties;

    public ApiKeyAuthenticationFilter(ApiKeyProperties apiKeyProperties) {
        this.apiKeyProperties = apiKeyProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String providedKey = request.getHeader(API_KEY_HEADER);
        String expectedKey = apiKeyProperties.getAdminApiKey();

        if (providedKey != null && !providedKey.isBlank() && expectedKey != null && !expectedKey.isBlank()
                && constantTimeEquals(providedKey, expectedKey)) {
            var authentication = new UsernamePasswordAuthenticationToken(
                    "admin-api-key",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8)
        );
    }
}
