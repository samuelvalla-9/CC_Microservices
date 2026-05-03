package org.citycare.facilityservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationClientFallback implements NotificationClient {

    @Override
    public void sendFacilityEvent(FacilityEventPayload event) {
        log.warn("NotificationService unavailable - facility event dropped: {}", event.eventType());
    }
}
