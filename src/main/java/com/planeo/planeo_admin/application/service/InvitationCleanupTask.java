package com.planeo.planeo_admin.application.service;

import com.planeo.planeo_admin.domain.entity.Invitation;
import com.planeo.planeo_admin.domain.enums.InvitationStatus;
import com.planeo.planeo_admin.domain.port.InvitationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Marks stale pending invitations as EXPIRED so a validity check never has to
 * rely solely on comparing timestamps at read time, and so admins listing
 * invitations later see an accurate status.
 */
@Component
public class InvitationCleanupTask {

    private final InvitationRepository invitationRepository;

    public InvitationCleanupTask(InvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expireStaleInvitations() {
        for (Invitation invitation : invitationRepository.findByStatusAndExpiresAtBefore(InvitationStatus.PENDING, Instant.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
        }
    }
}
