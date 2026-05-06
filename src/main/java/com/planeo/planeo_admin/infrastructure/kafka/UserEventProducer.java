package com.planeo.planeo_admin.infrastructure.kafka;

import com.planeo.planeo_admin.web.dto.UserEventDTO;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserEventProducer {
    private static final String TOPIC = "user.created";
    private final KafkaTemplate<String, UserEventDTO> template;

    public UserEventProducer(KafkaTemplate<String, UserEventDTO> template) {
        this.template = template;
    }

    public void publishUserCreated(UserEventDTO event) {
        template.send(TOPIC, event.username(), event);
    }
}
