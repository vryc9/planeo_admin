package com.planeo.planeo_admin.application.exception;

/**
 * Raised when an invitation exists but is no longer usable (already accepted or revoked).
 */
public class InvitationNotUsableException extends RuntimeException {
    public InvitationNotUsableException(String message) {
        super(message);
    }
}
