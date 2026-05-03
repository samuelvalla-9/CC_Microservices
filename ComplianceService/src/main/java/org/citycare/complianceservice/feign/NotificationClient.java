package org.citycare.complianceservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "NOTIFICATIONSERVICE", fallback = NotificationClientFallback.class)
public interface NotificationClient {

    @PostMapping("/notifications/events/compliance")
    void sendComplianceEvent(@RequestBody ComplianceEventPayload event);

    record ComplianceEventPayload(
        Long complianceId, Long entityId, String entityType, String result,
        String eventType, Long officerId, String findings, String recipientEmail
    ) {}
}
