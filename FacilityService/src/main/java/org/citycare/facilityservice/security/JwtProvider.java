package org.citycare.facilityservice.security;

import io.jsonwebtoken.Claims;
import org.citycare.security.jwt.JwtClaimsSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    public Claims getClaims(String token) {
        return JwtClaimsSupport.parseClaims(token, secret);
    }

    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}