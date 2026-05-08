package org.citycare.complianceservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${jwt.secret:Y2Y4ZTM1YmYtYjYyMC00ZDllLTlhZTMtZDY2ZDU4ZTMxZmE5Cg==}")
    private String secret;

    public Claims getClaims(String token) {
        // 1. Create the Key
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secret));

        // 2. Use parserBuilder() - This is the standard for 0.12.x
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}
