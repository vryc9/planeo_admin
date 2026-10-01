package com.planeo.planeo_admin.domain.port;

import com.planeo.planeo_admin.domain.entity.Invitation;
import com.planeo.planeo_admin.domain.enums.InvitationStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface InvitationRepository {
    Invitation save(Invitation invitation);
    Optional<Invitation> findById(Long id);
    Optional<Invitation> findByTokenHash(String tokenHash);
    List<Invitation> findByStatusAndExpiresAtBefore(InvitationStatus status, Instant instant);

    /** Replaces the creator's username by a placeholder on every invitation they issued. */
    void anonymizeCreator(String username);

    /** Forgets which user account accepted an invitation. */
    void detachAcceptedUser(Long userId);
}
