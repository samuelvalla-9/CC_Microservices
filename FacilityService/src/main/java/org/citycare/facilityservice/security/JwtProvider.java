package org.citycare.facilityservice.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.citycare.security.jwt.JwtClaimsSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * FacilityService's JWT provider wrapper.
 *
 * Delegates token parsing to centralized JwtClaimsSupport from jwt-shared-utils,
 * ensuring consistent cryptography across all services.
 */
@Component
@RequiredArgsConstructor
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Extract all claims from a JWT token.
     *
     * Uses JwtClaimsSupport for consistent, tested parsing logic.
     *
     * @param token JWT token
     * @return Parsed claims
     */
    public Claims getClaims(String token) {
        return JwtClaimsSupport.parseClaims(token, secret);
    }

    /**
     * Check if a token is expired.
     *
     * @param claims Token claims
     * @return True if token expiration date is before now
     */
    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}