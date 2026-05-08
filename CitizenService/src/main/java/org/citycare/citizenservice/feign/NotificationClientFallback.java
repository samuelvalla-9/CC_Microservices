package org.citycare.citizenservice.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class NotificationClientFallback implements NotificationClient {

    @Override
    public Object createNotification(Map<String, Object> request) {
        log.warn("NotificationService unavailable. Failed to send notification: {}", request.get("message"));
        return null;
    }

    @Override
    public Object documentEvent(Map<String, Object> request) {
        log.warn("NotificationService unavailable. Failed to send document event for citizen #{}", request.get("citizenId"));
        return null;
    }
}

