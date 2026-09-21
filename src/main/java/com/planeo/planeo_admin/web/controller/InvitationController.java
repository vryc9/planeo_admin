package com.planeo.planeo_admin.web.controller;

import com.planeo.planeo_admin.application.service.InvitationService;
import com.planeo.planeo_admin.web.dto.CreateInvitationDTO;
import com.planeo.planeo_admin.web.dto.InvitationCreatedDTO;
import com.planeo.planeo_admin.web.dto.InvitationPreviewDTO;
import com.planeo.planeo_admin.web.dto.RegisterFromInvitationDTO;
import com.planeo.planeo_admin.web.dto.UserDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    /**
     * Admin-only: triggers the whole invitation flow. Protected by the
     * X-Api-Key header (see SecurityConfig). Returns the one-time
     * registration link - the caller is responsible for delivering it to the
     * intended person; it is never sent by this service and never logged.
     */
    @PostMapping
    public ResponseEntity<InvitationCreatedDTO> create(@Valid @RequestBody CreateInvitationDTO dto,
                                                         Authentication authentication) {
        String createdBy = authentication != null ? authentication.getName() : "unknown";
        return ResponseEntity.status(HttpStatus.CREATED).body(invitationService.createInvitation(dto, createdBy));
    }

    /**
     * Admin-only: revokes a pending invitation before it is used.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(@PathVariable Long id) {
        invitationService.revokeInvitation(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Public: lets the registration front-end check a token before showing
     * the sign-up form. Rate-limited (see RateLimitingFilter).
     */
    @GetMapping("/validate/{token}")
    public ResponseEntity<InvitationPreviewDTO> preview(@PathVariable String token) {
        return ResponseEntity.ok(invitationService.previewInvitation(token));
    }
}
