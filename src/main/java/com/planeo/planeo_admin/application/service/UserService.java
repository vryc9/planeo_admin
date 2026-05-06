package com.planeo.planeo_admin.application.service;

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
        // Vérifie que le username n'existe pas déjà
        userRepository.findByUsername(dto.username())
                .ifPresent(u -> { throw new RuntimeException("Username already exists"); });

        // Sauvegarde dans la DB de planeo_admin
        User user = new User(dto.username(), Role.valueOf(dto.role()));
        User saved = userRepository.save(user);

        // Publie l'event sur Kafka
        userEventProducer.publishUserCreated(new UserEventDTO(
                dto.username(),
                dto.password(),
                dto.role()
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
