package org.citycare.facilityservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "NOTIFICATIONSERVICE", fallback = NotificationClientFallback.class)
public interface NotificationClient {

    @PostMapping("/notifications/events/facility")
    void sendFacilityEvent(@RequestBody FacilityEventPayload event);

    record FacilityEventPayload(
        Long facilityId, String facilityName, String eventType,
        Long staffId, String staffRole, Integer currentCapacity,
        Integer maxCapacity, Long notifyUserId, String recipientEmail
    ) {}
}
