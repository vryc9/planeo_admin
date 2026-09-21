package com.planeo.planeo_admin.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateInvitationDTO(
        @NotBlank @Email String email,
        @NotBlank String role
) {
}
