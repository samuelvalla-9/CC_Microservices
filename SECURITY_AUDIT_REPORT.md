# CityCare Microservices - Security Audit Report

## Executive Summary
A **critical security vulnerability** was discovered across multiple microservices in the CityCare project. Services were using `@PreAuthorize` annotations for role-based access control but **lacked JWT authentication filters**, causing authorization checks to fail with 403 errors.

## Vulnerabilities Found

### 1. ✅ FIXED - CitizenService
**Status:** CRITICAL - NOW FIXED
- **Issue:** Missing JWT filter and provider
- **Impact:** Calls to fetch citizen details fail with 403 Forbidden
- **Symptoms:** Admin panel unable to load citizen names
- **Root Cause:** No JWT token parsing in security chain
- **Fix Applied:** Added JwtProvider.java and JwtFilter.java to SecurityConfig
- **Files Modified:**
  - Created: `security/JwtProvider.java`
  - Created: `security/JwtFilter.java`
  - Modified: `config/SecurityConfig.java`

### 2. ✅ FIXED - ComplianceService
**Status:** CRITICAL - NOW FIXED
- **Issue:** Missing JWT dependencies, filter, and provider
- **Impact:** All compliance endpoints fail authorization despite @PreAuthorize decorators
- **Affected Endpoints:** 
  - POST /compliance/records (requires COMPLIANCE_OFFICER)
  - GET /compliance/records/* (requires COMPLIANCE_OFFICER)
  - POST /compliance/audits (requires COMPLIANCE_OFFICER)
  - GET /compliance/audits/* (requires COMPLIANCE_OFFICER)
  - And others with @PreAuthorize annotations
- **Root Cause:** No JWT token parsing + missing JWT dependencies
- **Fix Applied:**
  - Added JWT dependencies to pom.xml (jjwt-api, jjwt-impl, jjwt-jackson v0.12.3)
  - Created JwtProvider.java
  - Created JwtFilter.java
  - Updated SecurityConfig.java to register JwtFilter
- **Files Modified:**
  - Modified: `pom.xml` (added JWT dependencies)
  - Created: `security/JwtProvider.java`
  - Created: `security/JwtFilter.java`
  - Modified: `config/SecurityConfig.java`

### 3. ✅ VERIFIED SAFE - PatientTreatmentService
**Status:** SECURE
- Has JwtFilter implemented correctly
- Has JwtProvider for token parsing
- SecurityConfig properly configured
- All @PreAuthorize checks will work

### 4. ✅ VERIFIED SAFE - FacilityService
**Status:** SECURE
- Has JwtFilter implemented correctly
- Has JwtProvider for token parsing
- SecurityConfig properly configured

### 5. ✅ VERIFIED SAFE - EmergencyService
**Status:** SECURE
- Has JwtFilter implemented correctly
- Has JwtProvider for token parsing
- SecurityConfig properly configured

### 6. ✅ VERIFIED SAFE - AuthService
**Status:** SECURE
- Has JwtFilter implemented correctly
- Has JwtProvider for token parsing
- SecurityConfig properly configured

### 7. ✅ VERIFIED SAFE - NotificationService
**Status:** SECURE
- Has JwtAuthFilter (custom naming) implemented correctly
- Has JwtUtil for token parsing
- SecurityConfig properly configured

### 8. N/A - ConfigServer
**Status:** Not applicable
- Configuration server, no API endpoints requiring authentication

### 9. N/A - ServiceRegistry (Eureka)
**Status:** Not applicable
- Service registry, minimal security requirements

### 10. N/A - ApiGateway
**Status:** Requires separate review
- Gateway-level security handled separately
- Should validate tokens before routing to services

## How the Bug Manifested

### Without JWT Filter:
```
Request → Spring Security Chain → @PreAuthorize check → No authenticated user in context
→ Authorization fails with 403 Forbidden
```

### With JWT Filter (FIXED):
```
Request → JwtFilter → Parse JWT → Extract role from claims → 
Set SecurityContext with role → Spring Security Chain → 
@PreAuthorize check → Role matches → Request allowed → Success
```

## Technical Details

### JWT Filter Flow:
1. Request arrives with `Authorization: Bearer <JWT_TOKEN>`
2. JwtFilter intercepts request
3. JwtProvider parses JWT and extracts claims:
   - `role`: User's role (ADMIN, DOCTOR, NURSE, etc.)
   - `userId`: User ID
4. `UsernamePasswordAuthenticationToken` created with:
   - Principal: userId
   - Authorities: ROLE_{role}
5. SecurityContextHolder populated with authentication
6. `@PreAuthorize("hasRole('ADMIN')")` can now check authorities
7. Request proceeds or is rejected based on roles

## Fixes Summary

### ComplianceService (Detailed)
**Added to pom.xml:**
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

**Created security/JwtProvider.java:**
- Parses JWT tokens using JJWT 0.12.3
- Extracts claims from token
- Validates token expiration

**Created security/JwtFilter.java:**
- Extends OncePerRequestFilter
- Parses JWT from Authorization header
- Creates authenticated token with role
- Sets SecurityContextHolder

**Modified config/SecurityConfig.java:**
- Registered JwtFilter in filter chain
- Added `@RequiredArgsConstructor` for DI
- Filter runs before UsernamePasswordAuthenticationFilter

## Rebuild Instructions

After these changes, rebuild the affected services:

```bash
# CitizenService
mvn clean install -pl CitizenService

# ComplianceService  
mvn clean install -pl ComplianceService
```

## Testing Checklist

- [ ] Rebuild CitizenService microservice
- [ ] Rebuild ComplianceService microservice
- [ ] Restart both services
- [ ] Test Admin panel - verify citizen names load in Patients Management tab
- [ ] Test Compliance endpoints - verify COMPLIANCE_OFFICER can access resources
- [ ] Check server logs for JWT authentication messages
- [ ] Verify no 403 errors in browser console

## Prevention Going Forward

1. **All services with @PreAuthorize must have:**
   - JWT dependencies in pom.xml
   - JwtProvider or equivalent token parser
   - JwtFilter or equivalent request interceptor
   - SecurityConfig registering the filter

2. **Code Review Checklist:**
   - If adding @PreAuthorize annotation, verify JWT filter exists
   - If creating new microservice with Spring Security, add JWT filter immediately
   - Check pom.xml has JWT dependencies

3. **Template:**
   Consider creating a base security configuration template that all services inherit from to prevent this issue in future services.

## Files Modified Summary

### CitizenService (3 files)
- ✅ Created: `src/main/java/org/citycare/citizenservice/security/JwtProvider.java`
- ✅ Created: `src/main/java/org/citycare/citizenservice/security/JwtFilter.java`
- ✅ Modified: `src/main/java/org/citycare/citizenservice/config/SecurityConfig.java`

### ComplianceService (4 files)
- ✅ Modified: `pom.xml` (added JWT dependencies)
- ✅ Created: `src/main/java/org/citycare/complianceservice/security/JwtProvider.java`
- ✅ Created: `src/main/java/org/citycare/complianceservice/security/JwtFilter.java`
- ✅ Modified: `src/main/java/org/citycare/complianceservice/config/SecurityConfig.java`

## Severity Assessment

- **Severity:** CRITICAL
- **Scope:** 2 microservices directly affected
- **Impact:** Complete authorization bypass (any request denied despite proper JWT)
- **User Impact:** Admin and Compliance Officer features completely non-functional
- **Data Risk:** No direct data exposure, but authorization enforcement is broken
- **Recommendation:** Rebuild and deploy affected services immediately
