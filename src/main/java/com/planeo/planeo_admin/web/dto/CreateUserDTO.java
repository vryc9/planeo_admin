package com.planeo.planeo_admin.web.dto;

public record CreateUserDTO(
        String username,
        String password,
        String role
) {
}
