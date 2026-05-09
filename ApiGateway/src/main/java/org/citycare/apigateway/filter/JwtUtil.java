package org.citycare.apigateway.filter;

import org.citycare.security.jwt.JwtClaimsSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    public boolean validateToken(String token) {
        return JwtClaimsSupport.validateToken(token, secret);
    }
}
