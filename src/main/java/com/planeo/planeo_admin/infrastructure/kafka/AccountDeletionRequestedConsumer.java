package com.planeo.planeo_admin.infrastructure.kafka;

import com.planeo.planeo_admin.application.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Reacts to AccountDeletionRequested. Erasing then confirming is safe to replay: erasing an
 * absent user is a no-op and the confirmation is deduplicated by planeo_back. Any failure
 * propagates so the error handler retries, then routes the record to the DLT.
 */
@Component
public class AccountDeletionRequestedConsumer {

    static final String REQUESTED_TOPIC = "account.deletion.requested";
    static final String CONFIRMATION_TOPIC = "account.data.deleted";
    private static final Logger log = LoggerFactory.getLogger(AccountDeletionRequestedConsumer.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserService userService;
    private final KafkaTemplate<String, String> stringKafkaTemplate;

    public AccountDeletionRequestedConsumer(UserService userService, KafkaTemplate<String, String> stringKafkaTemplate) {
        this.userService = userService;
        this.stringKafkaTemplate = stringKafkaTemplate;
    }

    @KafkaListener(topics = REQUESTED_TOPIC, groupId = "planeo-admin-group")
    public void consume(String payload) throws Exception {
        AccountDeletionRequestedMessage event = parse(payload);

        userService.erase(event.username());

        String confirmation = objectMapper.writeValueAsString(
                new AccountDataDeletedMessage(event.requestId(), "ADMIN", Instant.now().toString()));
        stringKafkaTemplate.send(CONFIRMATION_TOPIC, event.requestId(), confirmation).get(10, TimeUnit.SECONDS);
        log.info("Compte supprimé dans planeo_admin (requestId={})", event.requestId());
    }

    private AccountDeletionRequestedMessage parse(String payload) {
        AccountDeletionRequestedMessage event;
        try {
            event = objectMapper.readValue(payload, AccountDeletionRequestedMessage.class);
        } catch (JacksonException e) {
            throw new InvalidMessageException("Payload illisible", e);
        }
        if (event == null || event.requestId() == null || event.requestId().isBlank()
                || event.username() == null || event.username().isBlank()) {
            throw new InvalidMessageException("account.deletion.requested incomplet", null);
        }
        return event;
    }
}
