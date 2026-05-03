package org.citycare.complianceservice.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationClientFallback implements NotificationClient {

    @Override
    public void sendComplianceEvent(ComplianceEventPayload event) {
        log.warn("NotificationService unavailable - compliance event dropped: {}", event.eventType());
    }
}
