package com.planeo.planeo_admin.application.service;

import com.planeo.planeo_admin.application.exception.InvalidRoleException;
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
import com.planeo.planeo_admin.web.dto.InvitationPreviewDTO;
import com.planeo.planeo_admin.web.dto.RegisterFromInvitationDTO;
import com.planeo.planeo_admin.web.dto.UserDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final UserService userService;
    private final InvitationTokenGenerator tokenGenerator;
    private final InvitationProperties invitationProperties;

    public InvitationService(InvitationRepository invitationRepository,
                              UserService userService,
                              InvitationTokenGenerator tokenGenerator,
                              InvitationProperties invitationProperties) {
        this.invitationRepository = invitationRepository;
        this.userService = userService;
        this.tokenGenerator = tokenGenerator;
        this.invitationProperties = invitationProperties;
    }

    @Transactional
    public InvitationCreatedDTO createInvitation(CreateInvitationDTO dto, String createdBy) {
        Role role = parseRole(dto.role());

        // Avoid several concurrently valid tokens for the same target: any
        // previously pending invitation for this email is superseded.
        invitationRepository.findByEmailAndStatus(dto.email(), InvitationStatus.PENDING)
                .forEach(pending -> {
                    pending.setStatus(InvitationStatus.REVOKED);
                    invitationRepository.save(pending);
                });

        String rawToken = tokenGenerator.generateToken();
        String tokenHash = tokenGenerator.hash(rawToken);
        Instant expiresAt = Instant.now().plus(invitationProperties.getExpirationHours(), ChronoUnit.HOURS);

        Invitation invitation = new Invitation(dto.email(), role, tokenHash, expiresAt, createdBy);
        Invitation saved = invitationRepository.save(invitation);

        String registrationLink = invitationProperties.getBaseUrl() + "/register?token=" + rawToken;

        return new InvitationCreatedDTO(saved.getId(), saved.getEmail(), role.name(), registrationLink, expiresAt);
    }

    @Transactional
    public InvitationPreviewDTO previewInvitation(String rawToken) {
        Invitation invitation = findUsableInvitationOrThrow(rawToken);
        return new InvitationPreviewDTO(invitation.getEmail(), invitation.getRole().name(), invitation.getExpiresAt());
    }

    @Transactional
    public UserDTO acceptInvitation(RegisterFromInvitationDTO dto) {
        Invitation invitation = findUsableInvitationOrThrow(dto.token());
        requirePasswordNotTrivial(dto.username(), dto.password());

        UserDTO created = userService.createUser(dto.username(), dto.password(), invitation.getRole());

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(Instant.now());
        invitation.setAcceptedUserId(created.id());
        invitationRepository.save(invitation);

        return created;
    }

    @Transactional
    public void revokeInvitation(Long id) {
        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new InvitationNotFoundException("Invitation not found"));
        invitation.setStatus(InvitationStatus.REVOKED);
        invitationRepository.save(invitation);
    }

    private Invitation findUsableInvitationOrThrow(String rawToken) {
        String tokenHash = tokenGenerator.hash(rawToken);
        Invitation invitation = invitationRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvitationNotFoundException("Invalid invitation token"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new InvitationNotUsableException("This invitation has already been used or revoked");
        }

        if (invitation.isExpired()) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new InvitationExpiredException("This invitation has expired");
        }

        return invitation;
    }

    /**
     * Length (enforced via bean validation) is the primary defense per current
     * password-guidance (NIST SP 800-63B): favour long passwords over forced
     * composition rules. This only rejects the one trivially weak case a
     * length check misses - the password simply being the username.
     */
    private void requirePasswordNotTrivial(String username, String password) {
        if (password.equalsIgnoreCase(username)) {
            throw new IllegalArgumentException("Password must not be the same as the username");
        }
    }

    private Role parseRole(String role) {
        try {
            return Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRoleException("Invalid role: " + role);
        }
    }
}
