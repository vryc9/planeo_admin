package com.planeo.planeo_admin.application.service;

import com.planeo.planeo_admin.application.exception.UsernameAlreadyExistsException;
import com.planeo.planeo_admin.domain.entity.User;
import com.planeo.planeo_admin.domain.enums.Role;
import com.planeo.planeo_admin.domain.port.UserRepository;
import com.planeo.planeo_admin.infrastructure.kafka.UserEventProducer;
import com.planeo.planeo_admin.web.dto.CreateUserDTO;
import com.planeo.planeo_admin.web.dto.UserDTO;
import com.planeo.planeo_admin.web.dto.UserEventDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserEventProducer userEventProducer;

    public UserService(UserRepository userRepository, UserEventProducer userEventProducer) {
        this.userRepository = userRepository;
        this.userEventProducer = userEventProducer;
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
}
