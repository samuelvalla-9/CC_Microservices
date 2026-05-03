package org.citycare.notificationservice.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback for NotificationClient — ensures other services don't fail
 * if NotificationService is temporarily down.
 */
@Component
@Slf4j
public class NotificationClientFallback implements NotificationClient {

    @Override
    public void sendEmergencyEvent(EmergencyEventPayload event) {
        log.warn("NotificationService unreachable — emergency event dropped for emergencyId: {}", event.emergencyId());
    }

    @Override
    public void sendPatientEvent(PatientEventPayload event) {
        log.warn("NotificationService unreachable — patient event dropped for patientId: {}", event.patientId());
    }

    @Override
    public void sendComplianceEvent(ComplianceEventPayload event) {
        log.warn("NotificationService unreachable — compliance event dropped for complianceId: {}", event.complianceId());
    }

    @Override
    public void sendAuthEvent(AuthEventPayload event) {
        log.warn("NotificationService unreachable — auth event dropped for userId: {}", event.userId());
    }

    @Override
    public void sendFacilityEvent(FacilityEventPayload event) {
        log.warn("NotificationService unreachable — facility event dropped for facilityId: {}", event.facilityId());
    }
}
