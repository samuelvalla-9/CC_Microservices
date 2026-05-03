package org.citycare.emergencyservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "NOTIFICATIONSERVICE", fallback = NotificationClientFallback.class)
public interface NotificationClient {

    @PostMapping("/notifications/events/emergency")
    void sendEmergencyEvent(@RequestBody EmergencyEventPayload event);

    record EmergencyEventPayload(
        Long emergencyId, Long citizenId, String type, String location,
        Long dispatcherId, String eventType, String newStatus, String recipientEmail
    ) {}
}
