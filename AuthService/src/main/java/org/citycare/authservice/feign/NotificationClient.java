package org.citycare.authservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "NOTIFICATIONSERVICE", fallback = NotificationClientFallback.class)
public interface NotificationClient {

    @PostMapping("/notifications/events/auth")
    void sendAuthEvent(@RequestBody AuthEventPayload event);

    record AuthEventPayload(Long userId, String name, String role, String eventType, String recipientEmail) {}
}
