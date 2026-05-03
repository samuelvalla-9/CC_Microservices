package org.citycare.patienttreatmentservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "NOTIFICATIONSERVICE", fallback = NotificationClientFallback.class)
public interface NotificationClient {

    @PostMapping("/notifications/events/patient")
    void sendPatientEvent(@RequestBody PatientEventPayload event);

    record PatientEventPayload(
        Long patientId, Long citizenId, Long facilityId, String eventType,
        String newStatus, String description, Long doctorId, String recipientEmail
    ) {}
}
