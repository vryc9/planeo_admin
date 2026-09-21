package com.planeo.planeo_admin.web.dto;

import java.time.Instant;

public record InvitationCreatedDTO(
        Long id,
        String role,
        String registrationLink,
        Instant expiresAt
) {
}
