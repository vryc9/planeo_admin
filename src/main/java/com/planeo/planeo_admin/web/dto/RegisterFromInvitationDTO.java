package com.planeo.planeo_admin.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterFromInvitationDTO(
        @NotBlank String token,

        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9_.-]{3,30}$", message = "Username must be 3-30 characters: letters, digits, dots, underscores or hyphens")
        String username,

        @NotBlank
        @Size(min = 12, max = 128, message = "Password must be between 12 and 128 characters")
        String password
) {
}
