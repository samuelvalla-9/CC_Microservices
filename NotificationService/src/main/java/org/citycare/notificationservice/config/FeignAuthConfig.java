package org.citycare.notificationservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Generates a service-level JWT for internal Feign calls so that
 * NotificationService can call protected endpoints (e.g. /admin/users/by-role)
 * without relying on the end-user's token.
 */
@Configuration
public class FeignAuthConfig {

    @Value("${jwt.secret}")
    private String secretKey;

    @Bean
    public RequestInterceptor serviceAuthInterceptor() {
        return (RequestTemplate template) -> {
            String token = generateServiceToken();
            template.header("Authorization", "Bearer " + token);
        };
    }

    private String generateServiceToken() {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));
        return Jwts.builder()
                .subject("notification-service")
                .claim("role", "ROLE_ADMIN")
                .claim("userId", 0)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000)) // 1 min validity
                .signWith(key)
                .compact();
    }
}
