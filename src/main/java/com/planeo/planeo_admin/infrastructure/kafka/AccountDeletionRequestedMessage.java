package com.planeo.planeo_admin.infrastructure.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Wire format of account.deletion.requested, published by planeo_back. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AccountDeletionRequestedMessage(String requestId, String username, String occurredAt) {
}
