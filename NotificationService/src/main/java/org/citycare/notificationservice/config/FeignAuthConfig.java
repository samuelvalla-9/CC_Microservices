package org.citycare.notificationservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.citycare.security.jwt.JwtTokenGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign interceptor configuration for NotificationService.
 *
 * Injects a service-level JWT into all Feign requests so that
 * NotificationService can call protected endpoints (e.g. /internal/users/by-role)
 * without relying on the end-user's token.
 *
 * The JWT is generated using the centralized JwtTokenGenerator from jwt-shared-utils,
 * ensuring consistent cryptography with the rest of the platform.
 *
 * Token details:
 * - Subject: "notification-service"
 * - Role: ROLE_ADMIN (privileged for internal calls)
 * - Expiration: 60 seconds (short-lived, regenerated per request)
 */
@Configuration
@RequiredArgsConstructor
public class FeignAuthConfig {

    private final JwtTokenGenerator jwtTokenGenerator;

    @Bean
    public RequestInterceptor serviceAuthInterceptor() {
        return (RequestTemplate template) -> {
            // Generate a fresh service token using the centralized generator
            // 60 second expiration: token is regenerated on every request
            String token = jwtTokenGenerator.generateServiceToken(
                "notification-service",
                60_000  // 60 seconds
            );
            template.header("Authorization", "Bearer " + token);
        };
    }
}
