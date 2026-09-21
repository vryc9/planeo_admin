package com.planeo.planeo_admin.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.time.Instant;

/**
 * Returns a plain 401 JSON body instead of Spring Security's default
 * redirect-to-login behaviour, since this API has no login form.
 */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        writeError(response, HttpStatus.UNAUTHORIZED, "Missing or invalid API key", request.getRequestURI());
    }

    static void writeError(HttpServletResponse response, HttpStatus status, String message, String path) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String json = """
                {"timestamp":"%s","status":%d,"error":"%s","message":"%s","path":"%s"}""".formatted(
                Instant.now(),
                status.value(),
                escape(status.getReasonPhrase()),
                escape(message),
                escape(path)
        );
        response.getWriter().write(json);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
