package org.citycare.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public final class JwtClaimsSupport {

    private JwtClaimsSupport() {
    }

    public static Claims parseClaims(String token, String base64UrlSecret) {
        return Jwts.parser()
                .verifyWith(getSignKey(base64UrlSecret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    public static boolean validateToken(String token, String base64UrlSecret) {
        try {
            Claims claims = parseClaims(token, base64UrlSecret);
            return !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    public static String extractUsername(String token, String base64UrlSecret) {
        return parseClaims(token, base64UrlSecret).getSubject();
    }

    public static String extractRole(String token, String base64UrlSecret) {
        return parseClaims(token, base64UrlSecret).get("role", String.class);
    }

    public static Long extractUserId(String token, String base64UrlSecret) {
        Object id = parseClaims(token, base64UrlSecret).get("userId");
        if (id instanceof Integer) {
            return ((Integer) id).longValue();
        }
        if (id instanceof Long) {
            return (Long) id;
        }
        if (id instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static SecretKey getSignKey(String base64UrlSecret) {
        byte[] keyBytes = Decoders.BASE64URL.decode(base64UrlSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

