package com.planeo.planeo_admin.infrastructure.kafka;

/** Wire format of account.data.deleted: confirms planeo_admin erased its data. */
public record AccountDataDeletedMessage(String requestId, String service, String deletedAt) {
}
