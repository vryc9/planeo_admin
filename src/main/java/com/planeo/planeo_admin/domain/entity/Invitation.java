package com.planeo.planeo_admin.domain.entity;

import com.planeo.planeo_admin.domain.enums.InvitationStatus;
import com.planeo.planeo_admin.domain.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@Entity
@Table(name = "invitations", indexes = {
        @Index(name = "idx_invitations_token_hash", columnList = "tokenHash", unique = true)
})
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * SHA-256 hex digest of the raw invitation token. The raw token itself is
     * never persisted: only the caller who received the registration link can
     * present it, so a database leak alone does not expose usable tokens.
     */
    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant acceptedAt;

    @Column(nullable = false, updatable = false)
    private String createdBy;

    private Long acceptedUserId;

    public Invitation() {
    }

    public Invitation(Role role, String tokenHash, Instant expiresAt, String createdBy) {
        this.role = role;
        this.tokenHash = tokenHash;
        this.status = InvitationStatus.PENDING;
        this.createdAt = Instant.now();
        this.expiresAt = expiresAt;
        this.createdBy = createdBy;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
