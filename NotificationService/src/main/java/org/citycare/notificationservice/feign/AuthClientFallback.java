package org.citycare.notificationservice.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class AuthClientFallback implements AuthClient {

    @Override
    public List<UserResponse> getUsersByRole(String role) {
        log.warn("AuthClient fallback triggered for getUsersByRole({})", role);
        return Collections.emptyList();
    }
}
