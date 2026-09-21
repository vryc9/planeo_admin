package com.planeo.planeo_admin.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "planeo.invitation")
public class InvitationProperties {

    /**
     * Public base URL of the registration front-end, e.g. https://planeo.example.com
     * The registration link is built as {baseUrl}/register?token=xxx
     */
    @NotBlank
    private String baseUrl;

    @Min(1)
    private long expirationHours = 48;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public long getExpirationHours() {
        return expirationHours;
    }

    public void setExpirationHours(long expirationHours) {
        this.expirationHours = expirationHours;
    }
}
