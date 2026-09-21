package com.planeo.planeo_admin.infrastructure.repository.adapter;

import com.planeo.planeo_admin.domain.entity.Invitation;
import com.planeo.planeo_admin.domain.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface JpaInvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByTokenHash(String tokenHash);
    List<Invitation> findByEmailAndStatus(String email, InvitationStatus status);
    List<Invitation> findByStatusAndExpiresAtBefore(InvitationStatus status, Instant instant);
}
