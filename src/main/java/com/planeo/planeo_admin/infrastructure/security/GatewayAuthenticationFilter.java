package com.planeo.planeo_admin.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Trusts the X-Auth-Username / X-Auth-Role headers set by planeo-gateway
 * after JWT validation. Safe ONLY because this service has no exposed port
 * and is reachable exclusively through the gateway on the internal Docker
 * network — never expose planeo-admin directly without revisiting this.
 */
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    private static final String USERNAME_HEADER = "X-Auth-Username";
    private static final String ROLE_HEADER = "X-Auth-Role";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String username = request.getHeader(USERNAME_HEADER);
        String role = request.getHeader(ROLE_HEADER);

        if (username != null && role != null) {
            var authentication = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}