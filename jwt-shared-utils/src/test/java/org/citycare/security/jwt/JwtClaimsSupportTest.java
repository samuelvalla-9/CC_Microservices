package org.citycare.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtClaimsSupportTest {

    @Test
    void parsesClaimsAndExtractsCommonFields() {
        Key key = Keys.hmacShaKeyFor("citycare-jwt-test-secret-key-citycare-123".getBytes());
        String secret = Encoders.BASE64URL.encode(key.getEncoded());

        String token = Jwts.builder()
                .subject("test-user")
                .claim("role", "ADMIN")
                .claim("userId", 99L)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();

        Claims claims = JwtClaimsSupport.parseClaims(token, secret);
        assertEquals("test-user", claims.getSubject());
        assertEquals("ADMIN", JwtClaimsSupport.extractRole(token, secret));
        assertEquals(99L, JwtClaimsSupport.extractUserId(token, secret));
        assertEquals("test-user", JwtClaimsSupport.extractUsername(token, secret));
        assertTrue(JwtClaimsSupport.validateToken(token, secret));
        assertNotNull(claims.getExpiration());
    }
}

