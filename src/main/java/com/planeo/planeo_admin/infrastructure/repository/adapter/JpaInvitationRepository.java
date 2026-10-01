package com.planeo.planeo_admin.infrastructure.repository.adapter;

import com.planeo.planeo_admin.domain.entity.Invitation;
import com.planeo.planeo_admin.domain.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface JpaInvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByTokenHash(String tokenHash);
    List<Invitation> findByStatusAndExpiresAtBefore(InvitationStatus status, Instant instant);

    @Modifying
    @Query("UPDATE Invitation i SET i.createdBy = :placeholder WHERE i.createdBy = :username")
    int replaceCreator(@Param("username") String username, @Param("placeholder") String placeholder);

    @Modifying
    @Query("UPDATE Invitation i SET i.acceptedUserId = NULL WHERE i.acceptedUserId = :userId")
    int clearAcceptedUser(@Param("userId") Long userId);
}
