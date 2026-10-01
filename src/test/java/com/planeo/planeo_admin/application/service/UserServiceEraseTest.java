package com.planeo.planeo_admin.application.service;

import com.planeo.planeo_admin.domain.entity.User;
import com.planeo.planeo_admin.domain.enums.Role;
import com.planeo.planeo_admin.domain.port.InvitationRepository;
import com.planeo.planeo_admin.domain.port.UserRepository;
import com.planeo.planeo_admin.infrastructure.kafka.UserEventProducer;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceEraseTest {

    private final UserRepository users = mock(UserRepository.class);
    private final InvitationRepository invitations = mock(InvitationRepository.class);
    private final UserService service = new UserService(users, mock(UserEventProducer.class), invitations);

    @Test
    void deletesUserAndAnonymizesInvitations() {
        User user = new User("alice", Role.valueOf("ADMIN"));
        user.setId(7L);
        when(users.findByUsername("alice")).thenReturn(Optional.of(user));

        service.erase("alice");

        verify(invitations).detachAcceptedUser(7L);
        verify(users).delete(user);
        verify(invitations).anonymizeCreator("alice");
    }

    @Test
    void isIdempotentWhenUserAlreadyErased() {
        when(users.findByUsername("alice")).thenReturn(Optional.empty());

        service.erase("alice");

        verify(users, never()).delete(any());
        verify(invitations, never()).detachAcceptedUser(any());
    }
}
