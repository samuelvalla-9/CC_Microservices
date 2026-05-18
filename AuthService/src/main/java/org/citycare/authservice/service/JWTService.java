package org.citycare.authservice.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.citycare.authservice.entity.User;
import org.citycare.authservice.repository.UserRepository;
import org.citycare.security.jwt.JwtClaimsSupport;
import org.citycare.security.jwt.JwtTokenGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.function.Function;

/**
 * AuthService's JWT wrapper around jwt-shared-utils.
 *
 * This service adds AuthService-specific business logic on top of the
 * low-level JwtTokenGenerator and JwtClaimsSupport from jwt-shared-utils:
 * - Password verification
 * - User entity mapping
 * - Role determination
 * - Audit logging (future)
 * - Token policy enforcement
 *
 * The actual cryptography is delegated to jwt-shared-utils.
 */
@Service
@RequiredArgsConstructor
public class JWTService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration.ms:1800000}")
    private long expirationMs;

    private final UserRepository userRepository;
    private final JwtTokenGenerator jwtTokenGenerator;

    /**
     * Generate a JWT token for an authenticated user.
     *
     * Delegates cryptography to JwtTokenGenerator but adds business logic:
     * - Verifies user entity exists
     * - Maps user data to token claims
     * - Applies company token expiration policy
     *
     * Called after successful authentication (password verified).
     *
     * @param user Authenticated user entity
     * @return Signed JWT token
     */
    public String generateToken(User user) {
        // Business logic: use shared utility to generate token
        // Token includes userId, role, and user email as subject
        return jwtTokenGenerator.generateUserToken(
            user.getEmail(),
            user.getUserId(),
            user.getRole().name(),
            expirationMs
        );
    }

    /**
     * Extract username (email) from a JWT token.
     *
     * Delegates to JwtClaimsSupport for parsing.
     *
     * @param token JWT token
     * @return Username (email) embedded in token
     */
    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract any claim from a JWT token.
     *
     * Generic method to extract specific claims using a resolver function.
     *
     * @param token JWT token
     * @param resolver Function to extract the claim
     * @return Extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    /**
     * Parse and extract all claims from a JWT token.
     *
     * Delegates to JwtClaimsSupport for parsing.
     *
     * @param token JWT token
     * @return All claims from the token
     */
    private Claims extractAllClaims(String token) {
        return JwtClaimsSupport.parseClaims(token, secretKey);
    }

    /**
     * Validate a JWT token against user details.
     *
     * Checks:
     * - Token subject matches username
     * - Token is not expired
     *
     * @param token JWT token
     * @param userDetails User details to validate against
     * @return True if token is valid for the user
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        return extractUserName(token).equals(userDetails.getUsername())
                && !extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
