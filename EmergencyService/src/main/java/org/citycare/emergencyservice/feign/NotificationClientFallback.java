package org.citycare.emergencyservice.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationClientFallback implements NotificationClient {

    @Override
    public void sendEmergencyEvent(EmergencyEventPayload event) {
        log.warn("NotificationService unavailable - emergency event dropped: {}", event.eventType());
    }
}
