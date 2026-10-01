package com.planeo.planeo_admin.application.service;

import com.planeo.planeo_admin.application.exception.UsernameAlreadyExistsException;
import com.planeo.planeo_admin.domain.entity.User;
import com.planeo.planeo_admin.domain.enums.Role;
import com.planeo.planeo_admin.domain.port.InvitationRepository;
import com.planeo.planeo_admin.domain.port.UserRepository;
import com.planeo.planeo_admin.infrastructure.kafka.UserEventProducer;
import com.planeo.planeo_admin.web.dto.CreateUserDTO;
import com.planeo.planeo_admin.web.dto.UserDTO;
import com.planeo.planeo_admin.web.dto.UserEventDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserEventProducer userEventProducer;
    private final InvitationRepository invitationRepository;

    public UserService(UserRepository userRepository, UserEventProducer userEventProducer,
                       InvitationRepository invitationRepository) {
        this.userRepository = userRepository;
        this.userEventProducer = userEventProducer;
        this.invitationRepository = invitationRepository;
    }

    public UserDTO create(CreateUserDTO dto) {
        return createUser(dto.username(), dto.password(), Role.valueOf(dto.role()));
    }

    /**
     * Shared by the direct-creation endpoint and the invitation-acceptance
     * flow. The role is always supplied by the trusted caller (either the
     * request body on the legacy endpoint, or the invitation record when
     * registering from an invitation) - never chosen by the registering user.
     */
    public UserDTO createUser(String username, String password, Role role) {
        userRepository.findByUsername(username)
                .ifPresent(u -> { throw new UsernameAlreadyExistsException("Username already exists"); });

        // Sauvegarde dans la DB de planeo_admin
        User user = new User(username, role);
        User saved = userRepository.save(user);

        // Publie l'event sur Kafka
        userEventProducer.publishUserCreated(new UserEventDTO(
                username,
                password,
                role.name()
        ));

        return new UserDTO(saved.getId(), saved.getUsername(), saved.getRole().name());
    }

    public List<UserDTO> findAll() {
        return userRepository.findAll()
                .stream()
                .map(u -> new UserDTO(u.getId(), u.getUsername(), u.getRole().name()))
                .toList();
    }

    public void delete(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        userRepository.delete(user);
    }

    /**
     * Erases the personal data held for a user (name and role) after an account deletion
     * request, and anonymizes the invitations that still point to them. Idempotent: a replayed
     * event on an already erased user does nothing.
     */
    @Transactional
    public void erase(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            invitationRepository.detachAcceptedUser(user.getId());
            userRepository.delete(user);
        });
        invitationRepository.anonymizeCreator(username);
    }
}
