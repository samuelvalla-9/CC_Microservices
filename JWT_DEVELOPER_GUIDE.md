# JWT Architecture - Developer Quick Reference

## TL;DR

JWT operations now go through **jwt-shared-utils**. 

**Three simple rules:**
1. To generate user tokens → Use `JwtTokenGenerator.generateUserToken()` (only AuthService does this)
2. To generate service tokens → Use `JwtTokenGenerator.generateServiceToken()` (NotificationService, future services)
3. To validate/parse tokens → Use `JwtClaimsSupport.*()` methods (all services do this)

---

## Where Things Live

| Component | Location | Role |
|-----------|----------|------|
| **JwtTokenGenerator** | `jwt-shared-utils/.../JwtTokenGenerator.java` | Generate all tokens |
| **JwtClaimsSupport** | `jwt-shared-utils/.../JwtClaimsSupport.java` | Parse & validate all tokens |
| **AuthService.JWTService** | `AuthService/.../service/JWTService.java` | User auth logic (calls JwtTokenGenerator) |
| **NotificationService config** | `NotificationService/.../config/FeignAuthConfig.java` | Service tokens (calls JwtTokenGenerator) |
| **All JwtProviders** | `*Service/.../security/JwtProvider.java` | Parse tokens (calls JwtClaimsSupport) |

---

## Common Tasks

### Task 1: Authenticate a User (AuthService only)

```java
// In AuthService
@Service
public class AuthService {
    @Autowired private JwtTokenGenerator jwtTokenGenerator;
    
    public String authenticateUser(User user) {
        // Generate token using centralized utility
        return jwtTokenGenerator.generateUserToken(
            user.getEmail(),              // subject
            user.getUserId(),             // user ID
            user.getRole().name(),        // role
            1800000                       // 30 minutes
        );
    }
}
```

### Task 2: Generate Inter-Service Token (Services that need it)

```java
// In NotificationService (or any service that needs to call protected endpoints)
@Configuration
public class FeignAuthConfig {
    @Autowired private JwtTokenGenerator jwtTokenGenerator;
    
    @Bean
    public RequestInterceptor serviceAuthInterceptor() {
        return (RequestTemplate template) -> {
            String token = jwtTokenGenerator.generateServiceToken(
                "notification-service",    // service name
                60_000                     // 60 seconds
            );
            template.header("Authorization", "Bearer " + token);
        };
    }
}
```

### Task 3: Extract Claims from Token (All services)

```java
// In any JWT Filter or security configuration
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;  // (wrapper around JwtClaimsSupport)
    
    protected void doFilterInternal(...) {
        String token = authHeader.substring(7);  // Remove "Bearer "
        
        try {
            // Parse token
            Claims claims = jwtProvider.getClaims(token);  // calls JwtClaimsSupport
            
            // Check expiration
            if (!jwtProvider.isTokenExpired(claims)) {  // calls JwtClaimsSupport
                String role = claims.get("role", String.class);
                Long userId = JwtClaimsSupport.extractUserId(token, secret);
                // Authenticate...
            }
        } catch (Exception e) {
            // Invalid token
        }
    }
}
```

---

## Dependency Injection

### If you're in a Spring service, autowire like this:

```java
@Component
public class MySecurityComponent {
    @Autowired private JwtTokenGenerator jwtTokenGenerator;  // For generation
    
    public void doSomething() {
        String token = jwtTokenGenerator.generateServiceToken("my-service", 60000);
    }
}
```

### If you're not in Spring, use static methods:

```java
// Static methods on JwtClaimsSupport (no autowiring needed)
String username = JwtClaimsSupport.extractUsername(token, secretKey);
boolean valid = JwtClaimsSupport.validateToken(token, secretKey);
```

---

## Common Imports

```java
// For token generation
import org.citycare.security.jwt.JwtTokenGenerator;

// For token parsing & validation
import org.citycare.security.jwt.JwtClaimsSupport;
import io.jsonwebtoken.Claims;

// For Spring integration
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
```

---

## What NOT to Do

❌ **DON'T** write your own JWT generation code:
```java
// WRONG - Don't do this
SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secret));
return Jwts.builder()
    .subject(username)
    .signWith(key)
    .compact();
```
✅ Use `JwtTokenGenerator` instead.

❌ **DON'T** duplicate parsing logic:
```java
// WRONG - Don't do this
Jwts.parser()
    .verifyWith(key)
    .build()
    .parseSignedClaims(token)
    .getPayload();
```
✅ Use `JwtClaimsSupport.parseClaims()` instead.

❌ **DON'T** add JJWT dependencies to your service pom.xml:
```xml
<!-- WRONG - Don't add this -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
</dependency>
```
✅ Just depend on `jwt-shared-utils`, you'll get JJWT transitively.

---

## Token Formats

### User Token (generated by AuthService)
```json
{
  "typ": "JWT",
  "alg": "HS256"
}
.
{
  "sub": "user@example.com",
  "userId": 123,
  "role": "CITIZEN",
  "iat": 1716100000,
  "exp": 1716101800
}
```

### Service Token (generated by services like NotificationService)
```json
{
  "typ": "JWT",
  "alg": "HS256"
}
.
{
  "sub": "notification-service",
  "userId": 0,
  "role": "ROLE_ADMIN",
  "serviceId": "notification-service",
  "iat": 1716100000,
  "exp": 1716100060
}
```

---

## Configuration

Add to your `application.yml` or `application.properties`:

```yaml
jwt:
  secret: ${JWT_SECRET}  # Base64URL-encoded key from ConfigServer or environment
  expiration:
    ms: 1800000  # 30 minutes for user tokens (5400000 = 90 min, 7200000 = 2 hours, etc.)
```

---

## Testing

### Unit Test Example:

```java
@SpringBootTest
public class JwtTokenGeneratorTest {
    
    @Autowired private JwtTokenGenerator generator;
    @Value("${jwt.secret}") private String secret;
    
    @Test
    public void testGenerateUserToken() {
        String token = generator.generateUserToken("user@example.com", 123L, "CITIZEN", 1800000);
        
        // Verify token can be parsed
        Claims claims = JwtClaimsSupport.parseClaims(token, secret);
        assertEquals("user@example.com", claims.getSubject());
        assertEquals(123L, claims.get("userId"));
        assertEquals("CITIZEN", claims.get("role"));
    }
    
    @Test
    public void testGenerateServiceToken() {
        String token = generator.generateServiceToken("test-service", 60000);
        
        // Verify token can be parsed
        Claims claims = JwtClaimsSupport.parseClaims(token, secret);
        assertEquals("test-service", claims.getSubject());
        assertEquals("ROLE_ADMIN", claims.get("role"));
    }
}
```

---

## FAQ

**Q: Can I still use individual service JwtProviders?**
A: Yes! They still exist and wrap `JwtClaimsSupport`. Use them if your service has local wrapper logic, or call `JwtClaimsSupport` directly if you prefer.

**Q: What if I need to add custom claims to a user token?**
A: Use `JwtTokenGenerator.generateUserToken(subject, userId, role, additionalClaims, expirationMs)`. Example:
```java
Map<String, Object> customClaims = new HashMap<>();
customClaims.put("department", "Emergency");
customClaims.put("facilityId", 42L);

String token = jwtTokenGenerator.generateUserToken(
    user.getEmail(), user.getUserId(), user.getRole().name(),
    customClaims,  // ← Pass custom claims here
    expirationMs
);
```

**Q: How do I change token expiration time?**
A: Modify `jwt.expiration.ms` in your ConfigServer or application properties. All tokens generated will use this value.

**Q: What happens if the JWT secret is wrong?**
A: `JwtClaimsSupport.validateToken()` returns false. Tokens are rejected during authentication.

**Q: Can multiple services use `JwtTokenGenerator`?**
A: Absolutely! Any service can generate service tokens for inter-service auth. Currently used by NotificationService; can be extended.

**Q: What about token refresh?**
A: Future enhancement. Use `JwtTokenGenerator.generateUserToken()` to create a new token with a fresh expiration timestamp.

---

## Support

For questions about JWT operations, refer to:
1. `JwtTokenGenerator` JavaDoc
2. `JwtClaimsSupport` JavaDoc
3. This guide
4. JWT_IMPLEMENTATION_SUMMARY.md (full architecture doc)

