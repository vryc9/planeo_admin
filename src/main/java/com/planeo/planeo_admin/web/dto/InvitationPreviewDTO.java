package com.planeo.planeo_admin.web.dto;

import java.time.Instant;

public record InvitationPreviewDTO(
        String email,
        String role,
        Instant expiresAt
) {
}
