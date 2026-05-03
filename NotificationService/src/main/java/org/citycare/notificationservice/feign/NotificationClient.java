package org.citycare.notificationservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for NotificationService.
 *
 * COPY this interface into any service (EmergencyService, PatientTreatmentService, etc.)
 * and change the package to match that service's package.
 *
 * Add @EnableFeignClients to the main class if not already present.
 *
 * Usage example in EmergencyServiceImpl:
 *
 *   @Autowired
 *   private NotificationClient notificationClient;
 *
 *   // After dispatching ambulance:
 *   EmergencyEventRequest event = new EmergencyEventRequest();
 *   event.setEmergencyId(emergency.getEmergencyId());
 *   event.setCitizenId(emergency.getCitizenId());
 *   event.setType(emergency.getType().name());
 *   event.setLocation(emergency.getLocation());
 *   event.setEventType("DISPATCHED");
 *   notificationClient.sendEmergencyEvent(event);
 */
@FeignClient(name = "NOTIFICATIONSERVICE", url = "${notification.service.url:}", fallback = NotificationClientFallback.class)
public interface NotificationClient {

    @PostMapping("/notifications/events/emergency")
    void sendEmergencyEvent(@RequestBody EmergencyEventPayload event);

    @PostMapping("/notifications/events/patient")
    void sendPatientEvent(@RequestBody PatientEventPayload event);

    @PostMapping("/notifications/events/compliance")
    void sendComplianceEvent(@RequestBody ComplianceEventPayload event);

    @PostMapping("/notifications/events/auth")
    void sendAuthEvent(@RequestBody AuthEventPayload event);

    @PostMapping("/notifications/events/facility")
    void sendFacilityEvent(@RequestBody FacilityEventPayload event);

    // ── Payload inner classes (or move to separate files) ─────────────────────

    record EmergencyEventPayload(
            Long emergencyId, Long citizenId, String type, String location,
            Long dispatcherId, String eventType, String newStatus, String recipientEmail
    ) {}

    record PatientEventPayload(
            Long patientId, Long citizenId, Long facilityId, String eventType,
            String newStatus, String description, Long doctorId, String recipientEmail
    ) {}

    record ComplianceEventPayload(
            Long complianceId, Long entityId, String entityType, String result,
            String eventType, Long officerId, String findings, String recipientEmail
    ) {}

    record AuthEventPayload(
            Long userId, String name, String role, String eventType, String recipientEmail
    ) {}

    record FacilityEventPayload(
            Long facilityId, String facilityName, String eventType, Long staffId,
            String staffRole, Integer currentCapacity, Integer maxCapacity,
            Long notifyUserId, String recipientEmail
    ) {}
}
