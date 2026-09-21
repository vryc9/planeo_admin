package com.planeo.planeo_admin.web.controller;

import com.planeo.planeo_admin.application.service.InvitationService;
import com.planeo.planeo_admin.web.dto.RegisterFromInvitationDTO;
import com.planeo.planeo_admin.web.dto.UserDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public endpoint backing the registration page reached via an invitation
 * link ({@code /register?token=xxx}). The role is never taken from the
 * request: it is fixed by whichever invitation the token resolves to.
 */
@RestController
public class RegistrationController {

    private final InvitationService invitationService;

    public RegistrationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterFromInvitationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invitationService.acceptInvitation(dto));
    }
}
