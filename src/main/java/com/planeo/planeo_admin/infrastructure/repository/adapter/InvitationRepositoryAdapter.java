package com.planeo.planeo_admin.infrastructure.repository.adapter;

import com.planeo.planeo_admin.domain.entity.Invitation;
import com.planeo.planeo_admin.domain.enums.InvitationStatus;
import com.planeo.planeo_admin.domain.port.InvitationRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class InvitationRepositoryAdapter implements InvitationRepository {
    private final JpaInvitationRepository repository;

    public InvitationRepositoryAdapter(JpaInvitationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Invitation save(Invitation invitation) {
        return repository.save(invitation);
    }

    @Override
    public Optional<Invitation> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Invitation> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash);
    }

    @Override
    public List<Invitation> findByStatusAndExpiresAtBefore(InvitationStatus status, Instant instant) {
        return repository.findByStatusAndExpiresAtBefore(status, instant);
    }
}
