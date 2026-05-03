package org.citycare.patienttreatmentservice.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationClientFallback implements NotificationClient {

    @Override
    public void sendPatientEvent(PatientEventPayload event) {
        log.warn("NotificationService unavailable - patient event dropped: {}", event.eventType());
    }
}
