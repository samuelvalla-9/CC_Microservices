package org.citycare.authservice.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationClientFallback implements NotificationClient {

    @Override
    public void sendAuthEvent(AuthEventPayload event) {
        log.warn("NotificationService unavailable - auth event dropped: {}", event.eventType());
    }
}
