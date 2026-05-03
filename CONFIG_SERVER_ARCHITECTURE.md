# Spring Cloud Config Server Architecture - CityCare Microservices

## Overview

The CityCare microservices project has been refactored to use **Spring Cloud Config Server**, a centralized configuration management system. This eliminates the need for each service to maintain its own `application.yml` file.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Config Repository                        │
│              (C:/Users/2478356/Downloads/...                │
│               CityCare-Microservices/config-repo)           │
│                                                              │
│  ├── application.yml (global shared config)                 │
│  ├── authservice.yml                                        │
│  ├── api-gateway.yml                                        │
│  ├── facilityservice.yml                                    │
│  ├── citizenservice.yml                                     │
│  ├── emergencyservice.yml                                   │
│  ├── complianceservice.yml                                  │
│  ├── patienttreatmentservice.yml                            │
│  └── notificationservice.yml                                │
└─────────────────────────────────────────────────────────────┘
                           ↑
                           │ reads from
                           │
┌──────────────────────────────────────────────────────────────┐
│                  ConfigServer                               │
│              (Port: 8888)                                   │
│   - spring-cloud-config-server dependency                  │
│   - Serves configurations to microservices                 │
│   - Exposed on endpoints like:                             │
│     /configserver/{app-name}/default/{label}              │
└──────────────────────────────────────────────────────────────┘
                           ↑
          ┌────────────────┼────────────────┐
          │                │                │
    ┌─────┴─────┐   ┌─────┴─────┐   ┌─────┴─────┐
    │ AuthService│   │ Facility  │   │   API     │
    │(Port 8086) │   │Service    │   │  Gateway  │
    │            │   │(Port 9099)│   │(Port 7070)│
    │ bootstrap. │   │           │   │           │
    │   yml      │   │bootstrap. │   │bootstrap. │
    │  ├─ Config │   │   yml     │   │   yml     │
    │  │ Server  │   │  ├─ Config│   │  ├─ Config│
    │  │  URI    │   │  │ Server │   │  │  Server│
    │  │ ├─ Port │   │  │  URI   │   │  │  URI   │
    │  │ │ 8888  │   │  │ ├─ Port│   │  │ ├─ Port│
    │  │ └─ URI  │   │  │ │ 8888 │   │  │ │ 8888 │
    │  └─────────┘   │  │ └──────│   │  │ └──────│
    │ application.   │  │applic. │   │  │applic. │
    │  yml (minimal) │  │  yml   │   │  │  yml   │
    │  (commented)   │  │(minimal)  │   │  (minimal)  │
    └────────────────┘   └──────────┘   └───────────┘
```

## How It Works

### 1. **Startup Sequence**

1. **Service Registry (Eureka)** starts first
   - Port: 8761
   - Service discovery hub

2. **ConfigServer** starts next
   - Port: 8888
   - Registers itself with Eureka
   - Loads configurations from the config-repo directory
   - Ready to serve configs to client services

3. **Microservices** start after ConfigServer is ready
   - Read `bootstrap.yml` BEFORE loading `application.yml`
   - Connect to ConfigServer using bootstrap.yml configuration
   - Fetch their service-specific configuration (e.g., `authservice.yml`)
   - Also fetch global `application.yml`
   - Merge configurations in order: global → service-specific → environment overrides
   - Then proceed with normal Spring Boot startup

### 2. **Bootstrap Configuration**

Each service now has a `bootstrap.yml` file (loaded before application.yml):

```yaml
spring:
  cloud:
    config:
      uri: http://localhost:8888          # ConfigServer address
      name: authservice                   # Which config file to load (authservice.yml)
      profile: default                    # Environment/profile
      label: main                         # Git branch (if using Git backend)
      fail-fast: true                     # Fail startup if ConfigServer unavailable
      retry:
        initial-interval: 1000            # Start retry after 1 second
        max-interval: 2000                # Max wait between retries
        max-attempts: 6                   # Retry up to 6 times
        multiplier: 1.1                   # Exponential backoff
      enabled: true
  application:
    name: AUTHSERVICE
```

**Key retry parameters for resilience:**
- If ConfigServer is down, services will retry 6 times with exponential backoff
- Total retry time: ~10 seconds before failing startup
- This gives you time to start ConfigServer in the correct order

### 3. **Configuration File Organization**

**Config Repository Structure:**
```
config-repo/
├── application.yml           # Global config (shared by all services)
├── authservice.yml          # AuthService-specific config
├── facilityservice.yml      # FacilityService-specific config
├── api-gateway.yml          # API Gateway-specific config
├── citizenservice.yml       # CitizenService-specific config
├── emergencyservice.yml     # EmergencyService-specific config
├── complianceservice.yml    # ComplianceService-specific config
├── patienttreatmentservice.yml  # PatientTreatmentService-specific config
└── notificationservice.yml  # NotificationService-specific config
```

**Configuration Merge Order (from lowest to highest priority):**
1. `application.yml` (global defaults)
2. `{service-name}.yml` (service-specific overrides)
3. Environment variables (highest priority)

### 4. **Services and Their Configurations**

| Service | Config File | Port | Key Config |
|---------|-------------|------|-----------|
| ConfigServer | application.yml | 8888 | native backend pointing to config-repo |
| AuthService | authservice.yml | 8086 | MySQL DB, JWT, Feign timeout |
| Facility Service | facilityservice.yml | 9099 | MySQL DB, JWT, Feign timeout |
| Citizen Service | citizenservice.yml | 8095 | MySQL DB, JWT, Feign timeout |
| Emergency Service | emergencyservice.yml | 8083 | MySQL DB, JWT, Feign timeout |
| Compliance Service | complianceservice.yml | 8084 | MySQL DB, Feign config |
| Patient Treatment Service | patienttreatmentservice.yml | 8082 | MySQL DB, JWT, Feign timeout |
| Notification Service | notificationservice.yml | 8089 | MySQL DB, JWT, Email config |
| API Gateway | api-gateway.yml | 7070 | CORS, Routes, Eureka |

## How to Start the System

### Step 1: Start Service Registry (Eureka)
```bash
cd ServiceRegistry
mvn spring-boot:run
# Available at http://localhost:8761
```

### Step 2: Start ConfigServer
```bash
cd ConfigServer
mvn spring-boot:run
# Available at http://localhost:8888
```

**Verify ConfigServer is working:**
```
http://localhost:8888/authservice/default
```

This should return a JSON response with the AuthService configuration.

### Step 3: Start Microservices (in any order, ConfigServer will serve configs)
```bash
# Example: Start AuthService
cd AuthService
mvn spring-boot:run

# Example: Start FacilityService
cd FacilityService
mvn spring-boot:run

# Start all others similarly
```

## Key Benefits

### ✅ **Centralized Management**
- All configurations in one location (config-repo)
- Easy to audit what each service uses
- Single source of truth

### ✅ **Reduced Redundancy**
- No more duplicate configuration code across services
- Global `application.yml` eliminates repetition
- Shared Eureka, Feign, management, logging configs

### ✅ **Environment Management**
- Different configs for dev, staging, production
- Can add `authservice-dev.yml`, `authservice-prod.yml` later
- Switch profiles without code changes

### ✅ **Dynamic Configuration Reload**
- (Future enhancement) Could implement `@RefreshScope` for runtime updates
- Use `/actuator/refresh` endpoint on each service
- Use Spring Cloud Bus for broadcast refreshes

### ✅ **Resilience & Retry Logic**
- Built-in retry mechanism in bootstrap.yml
- Exponential backoff prevents thundering herd
- fail-fast flag ensures early detection of config issues

### ✅ **Easier Deployment**
- Deploy new configurations without restarting services (with refresh)
- Scale services horizontally without config duplication
- Immutable Docker images (config comes from ConfigServer, not image)

## Configuration Loading Details

### When a Service Starts:

1. **Spring reads `bootstrap.yml`** (FIRST, before application context)
   - Determines ConfigServer location
   - Configures retry strategy
   
2. **Service connects to ConfigServer**
   - Request: `GET /authservice/default`
   - ConfigServer looks for: `authservice.yml` + `application.yml` in config-repo
   
3. **ConfigServer returns combined config**
   ```json
   {
     "name": "authservice",
     "profiles": ["default"],
     "label": "main",
     "version": null,
     "state": null,
     "propertySources": [
       {
         "name": "file:///c:/path/to/config-repo/authservice.yml",
         "source": { /* authservice.yml content */ }
       },
       {
         "name": "file:///c:/path/to/config-repo/application.yml",
         "source": { /* application.yml content */ }
       }
     ]
   }
   ```

4. **Service reads `application.yml`** (from project, usually empty now)
   - This allows local environment-specific overrides if needed
   
5. **Configuration merged in priority order**
   - Global (application.yml from ConfigServer)
   - Service-specific (authservice.yml from ConfigServer)
   - Local application.yml (rarely used now)
   - Environment variables (OS/Docker env vars)
   
6. **Service fully configured and starts**

## Changes Made to Your Project

### New Files Created:
- `ConfigServer/` - New microservice module
  - `pom.xml` - Maven configuration with spring-cloud-config-server
  - `ConfigServerApplication.java` - Main application class with @EnableConfigServer
  - `application.yml` - ConfigServer configuration
  
- `config-repo/` - Configuration repository
  - `application.yml` - Global shared configuration
  - `{service-name}.yml` - Individual service configurations (8 files)
  
- `{service}/src/main/resources/bootstrap.yml` - For each service (8 files)
  - Configures how each service connects to ConfigServer

### Modified Files:
- `pom.xml` (root) - Added ConfigServer to modules
- `{service}/pom.xml` (8 services) - Added `spring-cloud-starter-config` dependency
- `{service}/src/main/resources/application.yml` (8 services) - Cleared to minimal comments

## Testing the Configuration

### Test 1: Verify ConfigServer Serves Configs
```bash
curl http://localhost:8888/authservice/default
```

Should return AuthService configuration from `authservice.yml`.

### Test 2: Verify Service Gets Config
1. Start ConfigServer
2. Start a service (e.g., AuthService)
3. Check logs for messages like:
   ```
   Fetching config from server at : http://localhost:8888
   Located environment: name=authservice, profiles=[default], label=null
   ```

### Test 3: Check Eureka
- Auth service should show as registered: http://localhost:8761

### Test 4: Access Service Endpoints
- All service endpoints should work as before
- Example: `http://localhost:8086/auth/...` (AuthService)

## Troubleshooting

### Problem: "Connection refused to http://localhost:8888"
**Solution:** ConfigServer not running or not started yet
- Start ConfigServer first: `cd ConfigServer; mvn spring-boot:run`

### Problem: "fail-fast is true and service failed to fetch config"
**Solution:** Either ConfigServer unavailable or config file doesn't exist
- Check ConfigServer is running
- Verify config file exists in config-repo (e.g., `authservice.yml`)
- Check ConfigServer logs for errors

### Problem: Service uses wrong configuration
**Solution:** Configuration merge order issue
- Check bootstrap.yml for correct `name:` parameter
- Verify config file name matches (e.g., `authservice.yml` for AUTHSERVICE service)
- Remember: service-specific config overrides global config

### Problem: Changes to config-repo not picked up
**Solution:** ConfigServer caches configurations
- Restart ConfigServer to reload files
- (Advanced) Use Spring Cloud Bus for refresh without restart

## Future Enhancements

### 1. **Git Backend**
Instead of native file system, store configs in Git:
```yaml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/your-org/citycare-configs.git
          clone-on-start: true
```

### 2. **Encryption for Secrets**
```yaml
encrypt:
  key: myencryptionkey
```

Then in config files:
```yaml
jwt:
  secret: '{cipher}AQBYlyFQ4IH...'  # Encrypted at rest, decrypted on load
```

### 3. **Spring Cloud Bus for Live Refreshes**
Add to all services and use `/actuator/busrefresh` to push config changes to all services without restart.

### 4. **External Vault Integration**
Use HashiCorp Vault or AWS Secrets Manager for production secrets instead of storing in Git.

### 5. **Profile-Based Configurations**
Create environment-specific configs:
```
config-repo/
├── application.yml
├── application-dev.yml
├── application-prod.yml
├── authservice.yml
├── authservice-dev.yml
├── authservice-prod.yml
└── ... (repeat for all services)
```

## Summary

Your microservices now use **centralized configuration management**:

- **Single source of truth** for all configs in `config-repo/`
- **ConfigServer** (new service) distributes configs on demand
- **Each service** fetches its config on startup via `bootstrap.yml`
- **Zero duplication** - shared configs in `application.yml`
- **Environment-ready** - easy to add dev/staging/prod profiles later
- **Resilient** - built-in retry logic and error handling

This is a **production-ready architecture** following Spring Cloud best practices!

