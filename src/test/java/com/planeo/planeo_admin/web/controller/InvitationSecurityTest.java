package com.planeo.planeo_admin.web.controller;

import com.planeo.planeo_admin.application.service.InvitationService;
import com.planeo.planeo_admin.infrastructure.config.ApiKeyProperties;
import com.planeo.planeo_admin.infrastructure.security.SecurityConfig;
import com.planeo.planeo_admin.web.dto.InvitationCreatedDTO;
import com.planeo.planeo_admin.web.dto.InvitationPreviewDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that the shared-secret API key actually gates the admin-only
 * invitation endpoints, and that the public token-validation endpoint stays
 * reachable without one.
 */
@WebMvcTest(controllers = {InvitationController.class, RegistrationController.class})
@Import(SecurityConfig.class)
@EnableConfigurationProperties(ApiKeyProperties.class)
@TestPropertySource(properties = "planeo.security.admin-api-key=test-admin-key")
class InvitationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InvitationService invitationService;

    @Test
    void createInvitationWithoutApiKeyIsRejected() throws Exception {
        mockMvc.perform(post("/invitations")
                        .contentType("application/json")
                        .content("{\"email\":\"bob@example.com\",\"role\":\"USER\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createInvitationWithWrongApiKeyIsRejected() throws Exception {
        // A wrong key never authenticates (only an exact match does), so the
        // request stays anonymous and is rejected the same way as a missing key.
        mockMvc.perform(post("/invitations")
                        .header("X-Api-Key", "not-the-right-key")
                        .contentType("application/json")
                        .content("{\"email\":\"bob@example.com\",\"role\":\"USER\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createInvitationWithValidApiKeySucceeds() throws Exception {
        when(invitationService.createInvitation(any(), anyString())).thenReturn(
                new InvitationCreatedDTO(1L, "bob@example.com", "USER",
                        "https://planeo.example.com/register?token=abc", Instant.now()));

        mockMvc.perform(post("/invitations")
                        .header("X-Api-Key", "test-admin-key")
                        .contentType("application/json")
                        .content("{\"email\":\"bob@example.com\",\"role\":\"USER\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void revokeInvitationWithoutApiKeyIsRejected() throws Exception {
        mockMvc.perform(delete("/invitations/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validateInvitationTokenIsPubliclyAccessible() throws Exception {
        when(invitationService.previewInvitation("some-token")).thenReturn(
                new InvitationPreviewDTO("bob@example.com", "USER", Instant.now()));

        mockMvc.perform(get("/invitations/validate/some-token"))
                .andExpect(status().isOk());
    }

    @Test
    void registerEndpointIsPubliclyAccessible() throws Exception {
        mockMvc.perform(post("/register")
                        .contentType("application/json")
                        .content("{\"token\":\"t\",\"username\":\"bob\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest());
    }
}
