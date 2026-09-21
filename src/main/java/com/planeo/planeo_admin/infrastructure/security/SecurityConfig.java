package com.planeo.planeo_admin.infrastructure.security;

import com.planeo.planeo_admin.infrastructure.config.ApiKeyProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Minimal security setup, scoped to the invitation feature only.
 *
 * Every other endpoint keeps its current (unauthenticated) behaviour on
 * purpose: this is not a rewrite of the service's security posture, only the
 * protection needed for the new admin-triggered invitation endpoints. A full
 * authentication/authorization overhaul is left for a later, dedicated pass.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ApiKeyProperties apiKeyProperties) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/invitations").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/invitations/*").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                .addFilterBefore(new ApiKeyAuthenticationFilter(apiKeyProperties), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                        .accessDeniedHandler(new RestAccessDeniedHandler())
                );
        return http.build();
    }
}
