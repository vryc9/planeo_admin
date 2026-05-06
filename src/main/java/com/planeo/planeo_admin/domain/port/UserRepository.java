package com.planeo.planeo_admin.domain.port;

import com.planeo.planeo_admin.domain.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findByUsername(String username);
    List<User> findAll();
    void delete(User user);
}
