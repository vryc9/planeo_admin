package com.planeo.planeo_admin.infrastructure.kafka;

public class InvalidMessageException extends RuntimeException {
    public InvalidMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
