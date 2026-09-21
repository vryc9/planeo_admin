package com.planeo.planeo_admin.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "planeo.security")
public class ApiKeyProperties {

    /**
     * Shared secret required in the X-Api-Key header to call admin-only
     * endpoints (e.g. invitation creation/revocation). Must be provided via
     * environment variable in every real environment; never hardcode it.
     */
    @NotBlank
    private String adminApiKey;

    public String getAdminApiKey() {
        return adminApiKey;
    }

    public void setAdminApiKey(String adminApiKey) {
        this.adminApiKey = adminApiKey;
    }
}
