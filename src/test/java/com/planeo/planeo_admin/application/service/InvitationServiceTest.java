package com.planeo.planeo_admin.application.service;

import com.planeo.planeo_admin.application.exception.InvitationExpiredException;
import com.planeo.planeo_admin.application.exception.InvitationNotFoundException;
import com.planeo.planeo_admin.application.exception.InvitationNotUsableException;
import com.planeo.planeo_admin.domain.entity.Invitation;
import com.planeo.planeo_admin.domain.enums.InvitationStatus;
import com.planeo.planeo_admin.domain.enums.Role;
import com.planeo.planeo_admin.domain.port.InvitationRepository;
import com.planeo.planeo_admin.infrastructure.config.InvitationProperties;
import com.planeo.planeo_admin.infrastructure.security.InvitationTokenGenerator;
import com.planeo.planeo_admin.web.dto.CreateInvitationDTO;
import com.planeo.planeo_admin.web.dto.InvitationCreatedDTO;
import com.planeo.planeo_admin.web.dto.RegisterFromInvitationDTO;
import com.planeo.planeo_admin.web.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private UserService userService;

    private InvitationTokenGenerator tokenGenerator;
    private InvitationProperties invitationProperties;
    private InvitationService invitationService;

    @BeforeEach
    void setUp() {
        tokenGenerator = new InvitationTokenGenerator();
        invitationProperties = new InvitationProperties();
        invitationProperties.setBaseUrl("https://planeo.example.com");
        invitationProperties.setExpirationHours(48);
        invitationService = new InvitationService(invitationRepository, userService, tokenGenerator, invitationProperties);
    }

    @Test
    void createInvitationBuildsRegistrationLink() {
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvitationCreatedDTO result = invitationService.createInvitation(new CreateInvitationDTO("USER"), "admin");

        assertThat(result.registrationLink()).startsWith("https://planeo.example.com/register?token=");
        assertThat(result.role()).isEqualTo("USER");
    }

    @Test
    void previewRejectsUnknownToken() {
        when(invitationRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invitationService.previewInvitation("does-not-exist"))
                .isInstanceOf(InvitationNotFoundException.class);
    }

    @Test
    void previewRejectsExpiredTokenAndMarksItExpired() {
        Invitation invitation = new Invitation(Role.USER, "hash",
                Instant.now().minus(1, ChronoUnit.HOURS), "admin");
        when(invitationRepository.findByTokenHash(anyString())).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.previewInvitation("raw-token"))
                .isInstanceOf(InvitationExpiredException.class);

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.EXPIRED);
        verify(invitationRepository).save(invitation);
    }

    @Test
    void previewRejectsAlreadyAcceptedToken() {
        Invitation invitation = new Invitation(Role.USER, "hash",
                Instant.now().plus(1, ChronoUnit.DAYS), "admin");
        invitation.setStatus(InvitationStatus.ACCEPTED);
        when(invitationRepository.findByTokenHash(anyString())).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.previewInvitation("raw-token"))
                .isInstanceOf(InvitationNotUsableException.class);
    }

    @Test
    void acceptInvitationCreatesUserWithInvitationRoleNotAnyUserSuppliedRole() {
        Invitation invitation = new Invitation(Role.ADMIN, "hash",
                Instant.now().plus(1, ChronoUnit.DAYS), "admin");
        when(invitationRepository.findByTokenHash(anyString())).thenReturn(Optional.of(invitation));
        when(userService.createUser(eq("bob"), eq("Str0ng!Passw0rd"), eq(Role.ADMIN)))
                .thenReturn(new UserDTO(1L, "bob", "ADMIN"));

        UserDTO result = invitationService.acceptInvitation(
                new RegisterFromInvitationDTO("raw-token", "bob", "Str0ng!Passw0rd"));

        assertThat(result.role()).isEqualTo("ADMIN");
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        assertThat(invitation.getAcceptedUserId()).isEqualTo(1L);
        verify(userService, never()).createUser(anyString(), anyString(), eq(Role.USER));
    }

    @Test
    void acceptInvitationRejectsPasswordEqualToUsername() {
        Invitation invitation = new Invitation(Role.USER, "hash",
                Instant.now().plus(1, ChronoUnit.DAYS), "admin");
        when(invitationRepository.findByTokenHash(anyString())).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.acceptInvitation(
                new RegisterFromInvitationDTO("raw-token", "bob", "bob")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
