package com.planeo.planeo_admin.web.advice;

import com.planeo.planeo_admin.application.exception.InvalidRoleException;
import com.planeo.planeo_admin.application.exception.InvitationExpiredException;
import com.planeo.planeo_admin.application.exception.InvitationNotFoundException;
import com.planeo.planeo_admin.application.exception.InvitationNotUsableException;
import com.planeo.planeo_admin.application.exception.UsernameAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Central error mapping for the invitation/registration flow. Existing
 * endpoints (UserController) are intentionally left untouched.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request, details);
    }

    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<ApiError> handleInvalidRole(InvalidRoleException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(InvitationNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(InvitationNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(InvitationExpiredException.class)
    public ResponseEntity<ApiError> handleExpired(InvitationExpiredException ex, HttpServletRequest request) {
        return build(HttpStatus.GONE, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(InvitationNotUsableException.class)
    public ResponseEntity<ApiError> handleNotUsable(InvitationNotUsableException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUsernameConflict(UsernameAlreadyExistsException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, List.of());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest request, List<String> details) {
        ApiError body = new ApiError(status.value(), status.getReasonPhrase(), message, request.getRequestURI(), details);
        return ResponseEntity.status(status).body(body);
    }
}
