package com.planeo.planeo_admin.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateInvitationDTO(
        @NotBlank String role
) {
}
