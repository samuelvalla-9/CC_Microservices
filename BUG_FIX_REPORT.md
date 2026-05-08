# CityCare Bug Fix: CitizenService Missing JWT Authentication

## Problem
The admin panel was unable to load citizen names when viewing patients, resulting in "Unknown Citizen" being displayed in the "Reported By" column. The frontend was receiving 403 (Forbidden) errors when calling the CitizenService endpoint to fetch citizen details.

## Root Cause
The **CitizenService microservice was missing JWT authentication filters**, which are required to:
1. Parse and validate JWT tokens from incoming requests
2. Extract user roles from the JWT claims
3. Set up Spring Security context for `@PreAuthorize` annotations to work

The `getById()` endpoint in CitizenController has the restriction:
```java
@GetMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ApiResponse<Citizen>> getById(@PathVariable Long id) { ... }
```

Without the JWT filter, the `@PreAuthorize("hasRole('ADMIN')")` check failed because:
- No JWT token was being parsed
- No authentication was set in the Security Context
- Spring Security couldn't find the user's role
- Request was rejected with 403 Forbidden

## Solution
Added three missing components to CitizenService:

### 1. JwtProvider.java
- Parses JWT tokens and extracts claims
- Uses JJWT 0.12.6 API (matching pom.xml)
- Validates token expiration
- Created at: `CitizenService/src/main/java/org/citycare/citizenservice/security/JwtProvider.java`

### 2. JwtFilter.java
- Implements `OncePerRequestFilter` to intercept all requests
- Extracts JWT token from Authorization header
- Parses token claims and retrieves role and userId
- Creates `UsernamePasswordAuthenticationToken` with ROLE_ prefix
- Sets authentication in Spring Security context
- Handles errors gracefully
- Created at: `CitizenService/src/main/java/org/citycare/citizenservice/security/JwtFilter.java`

### 3. Updated SecurityConfig.java
- Registered JwtFilter in the security filter chain
- Added `@RequiredArgsConstructor` for dependency injection
- Configured filter to run before `UsernamePasswordAuthenticationFilter`

## How It Works Now
1. Frontend sends request with `Authorization: Bearer <JWT_TOKEN>`
2. JwtFilter intercepts the request
3. JwtProvider parses the JWT token
4. User's role is extracted from token claims
5. `UsernamePasswordAuthenticationToken` is created with role as `ROLE_ADMIN`
6. Security context is set with this authentication
7. `@PreAuthorize("hasRole('ADMIN')")` check passes
8. CitizenService endpoint returns citizen details successfully
9. Frontend displays citizen names in admin panel

## Files Modified/Created
- ✅ Created: `CitizenService/src/main/java/org/citycare/citizenservice/security/JwtProvider.java`
- ✅ Created: `CitizenService/src/main/java/org/citycare/citizenservice/security/JwtFilter.java`
- ✅ Modified: `CitizenService/src/main/java/org/citycare/citizenservice/config/SecurityConfig.java`

## Testing
After rebuilding the CitizenService microservice, the admin panel should now:
1. Successfully fetch citizen names via `/api/citizens/{id}` endpoint
2. Display citizen names in the "Reported By" column instead of "Unknown Citizen"
3. No more 403 errors in the browser console
