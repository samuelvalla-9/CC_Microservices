                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               # CityCare Microservices - ConfigServer Architecture 🏥

> Centralized Configuration Management for Spring Boot Microservices

## 🎯 Quick Overview

Your CityCare microservices project has been **completely refactored** to use **Spring Cloud Config Server** for centralized configuration management. This eliminates configuration duplication and provides a production-ready architecture.

### What You Get
- ✅ **Centralized Configs** - All settings in one place (`config-repo/`)
- ✅ **No Duplication** - Shared global configs across all services
- ✅ **Production Ready** - Built-in retry logic & resilience
- ✅ **Easy Scaling** - Add services without duplicating configurations
- ✅ **Environment Support** - Dev/staging/prod profiles ready
- ✅ **Future-Ready** - Git backend, encryption, vault integration ready

---

## 🚀 Getting Started (5 minutes)

### Step 1: Start Service Registry
```bash
cd ServiceRegistry
mvn spring-boot:run
# Available at: http://localhost:8761
```

### Step 2: Start ConfigServer
```bash
cd ConfigServer
mvn spring-boot:run
# Available at: http://localhost:8888
# Verify: curl http://localhost:8888/authservice/default
```

### Step 3: Start Microservices (Any Order)
```bash
cd AuthService
mvn spring-boot:run

# In other terminals:
cd FacilityService
mvn spring-boot:run

# ... repeat for other services
```

**That's it!** All services are now configured from the central ConfigServer.

👉 **Detailed guide:** See [`QUICKSTART.md`](QUICKSTART.md)

---

## 📚 Documentation

### New Features & Architecture
- **📖 [CONFIG_SERVER_ARCHITECTURE.md](CONFIG_SERVER_ARCHITECTURE.md)** - Complete deep-dive into how the system works
  - Architecture diagrams
  - Startup sequence
  - Configuration loading details
  - Troubleshooting guide

### How to Use It
- **📖 [QUICKSTART.md](QUICKSTART.md)** - Start the system step by step
  - Startup order (IMPORTANT!)
  - Port reference
  - Common issues
  - Verification checklist

- **📖 [CONFIGURATION_GUIDE.md](CONFIGURATION_GUIDE.md)** - Modify and manage configurations
  - How to change settings
  - Adding new services
  - Environment-specific configs
  - Database/JWT/email configuration examples

### Migration Info
- **📖 [MIGRATION_SUMMARY.md](MIGRATION_SUMMARY.md)** - What changed in your project
  - Files created/modified
  - Before vs after comparison
  - Key improvements

---

## 🏗️ Project Structure

```
CityCare-Microservices/
├── ServiceRegistry/                    # Eureka service discovery
├── ConfigServer/ ← NEW!               # Centralized config management
│   ├── pom.xml
│   └── src/main/resources/application.yml
│
├── config-repo/ ← NEW!                # All configurations here
│   ├── application.yml                # Global shared config
│   ├── authservice.yml
│   ├── facilityservice.yml
│   ├── api-gateway.yml
│   ├── citizenservice.yml
│   ├── emergencyservice.yml
│   ├── complianceservice.yml
│   ├── patienttreatmentservice.yml
│   └── notificationservice.yml
│
├── AuthService/
│   ├── pom.xml (+ spring-cloud-starter-config)
│   └── src/main/resources/
│       ├── bootstrap.yml ← NEW!       # ConfigServer connection
│       └── application.yml            # Cleared (config from server)
│
├── ApiGateway/
│   ├── bootstrap.yml ← NEW!           # Same pattern for all services
│   └── application.yml
│
├── FacilityService/
├── CitizenService/
├── EmergencyService/
├── ComplianceService/
├── PatientTreatmentService/
├── NotificationService/
│
├── SpringAdminService/                # Admin dashboard (unchanged)
├── pom.xml (root)                    # Added ConfigServer module
└── README.md (THIS FILE)

# Documentation files
├── CONFIG_SERVER_ARCHITECTURE.md      # How it all works
├── QUICKSTART.md                      # How to run it
├── CONFIGURATION_GUIDE.md             # How to modify configs
├── MIGRATION_SUMMARY.md               # What changed
└── README.md                          # You are here
```

---

## ⚙️ How It Works

### Configuration Flow
```
Service Startup
    ↓
[1] Read bootstrap.yml (ConfigServer connection config)
    ↓
[2] Connect to ConfigServer @ http://localhost:8888
    ↓
[3] Request service-specific config (e.g., authservice.yml)
    ↓
[4] Merge: Global (application.yml) + Service-specific + Environment vars
    ↓
[5] Read local application.yml (usually empty now)
    ↓
[6] Service fully configured ✓
```

### Key Files

**Each service now needs:**
1. `bootstrap.yml` - Tells service where to find configurations
2. `application.yml` - Can be empty/minimal (configs come from server)

**Central repo:**
1. `config-repo/application.yml` - Global config for all services
2. `config-repo/{service-name}.yml` - Service-specific overrides

---

## 🔧 Changing Configuration

### Change Database URL
```yaml
# In config-repo/facilityservice.yml
spring:
  datasource:
    url: jdbc:mysql://new-host:3306/new_database
```
ConfigServer automatically serves the new config. No restart needed!

### Add New Environment (Dev/Staging/Prod)
```bash
# Create new config files
config-repo/
├── application-dev.yml    # Dev overrides
├── application-prod.yml   # Prod overrides
├── authservice-dev.yml
├── authservice-prod.yml
└── ...
```

Then start with profile: `mvn spring-boot:run -Dspring.profiles.active=prod`

### Add New Service
1. Add `bootstrap.yml` to new service
2. Add new config file: `config-repo/newservice.yml`
3. Start it - ConfigServer serves the config automatically!

👉 **Full guide:** See [`CONFIGURATION_GUIDE.md`](CONFIGURATION_GUIDE.md)

---

## 🎯 Key Components

### ConfigServer
- **Role:** Central repository for all configurations
- **Port:** 8888
- **Backend:** Local filesystem (config-repo/)
- **Can upgrade to:** Git, Vault, or other backends
- **Startup:** Must start before client services
- **Provides:** Configurations on-demand via REST API

### bootstrap.yml (in each service)
- **Role:** Configuration for how to connect to ConfigServer
- **Loaded:** BEFORE application.yml
- **Contains:** ConfigServer URL, retry logic, service name
- **Usually:** Never changes after initial setup

### config-repo/ (Central Configuration)
- **Location:** `C:/Users/2478356/Downloads/CityCare-Microservices/config-repo/`
- **Files:** YAML configuration files 
- **Format:** `{service-name}.yml` for service-specific
- **Format:** `application.yml` for global/shared

---

## 🚨 Important: Startup Order

You **MUST** start services in this order:

```
1. ServiceRegistry (Eureka) - port 8761
   └─ Provides service discovery
   
2. ConfigServer - port 8888
   └─ Provides configurations
   
3. All Microservices - any order now
   ├─ AuthService - port 8086
   ├─ FacilityService - port 9099
   ├─ CitizenService - port 8095
   ├─ EmergencyService - port 8083
   ├─ ComplianceService - port 8084
   ├─ PatientTreatmentService - port 8082
   ├─ NotificationService - port 8089
   └─ API Gateway - port 7070
```

**Why?** Services need ConfigServer ready when they start. If you start a service before ConfigServer, it will retry/fail.

---

## 📊 Benefits

| Feature | Before | After |
|---------|--------|-------|
| Config Location | Each service | Central (config-repo/) |
| Duplication | High | Low |
| Shared Settings | Copy-paste | Inherited from global |
| Environment Configs | Manual per service | Profile-based system |
| Adding Services | Duplicate existing configs | Copy one template |
| Changing Config | Edit each service's yml | Edit one file + refresh |
| Version Control Ready | No | Yes (Git backend) |
| Encrypted Secrets | No | Ready (Spring Cloud encryption) |
| Scaling Complexity | Increases per service | Linear (ConfigServer) |

---

## 🧪 Testing

### Test 1: ConfigServer Serving Configs
```bash
curl http://localhost:8888/authservice/default
# Should return JSON with AuthService configuration
```

### Test 2: Service Using Config
```bash
# Check that service used the config from ConfigServer
curl http://localhost:8086/actuator/env
# Look for property sources - should show ConfigServer
```

### Test 3: Eureka Registration
```
http://localhost:8761
# All services should be listed (green)
```

---

## 🔒 Security & Production

### Current Setup (Development)
- ✅ ConfigServer on localhost
- ✅ Configs in plaintext YAML files
- ✅ Local filesystem backend

### For Production, Add:
1. **Encryption** - Encrypt sensitive properties in config files
2. **Vault Integration** - Use HashiCorp Vault or AWS Secrets Manager
3. **Git Backend** - Store configs in Git for version control
4. **Access Control** - Restrict who can read/modify ConfigServer
5. **Spring Cloud Bus** - Push config changes without restart

👉 **Full security guide:** See [`CONFIGURATION_GUIDE.md`](CONFIGURATION_GUIDE.md) (Production Checklist section)

---

## 🐛 Troubleshooting

### "Connection refused to localhost:8888"
**Problem:** ConfigServer not running  
**Solution:** Start ConfigServer first: `cd ConfigServer; mvn spring-boot:run`

### "fail-fast is true and couldn't load config"
**Problem:** ConfigServer not available or config file missing  
**Solution:** Check ConfigServer is running, verify config file exists

### "Service using wrong port/database"
**Problem:** Configuration merge order issue  
**Solution:** Check bootstrap.yml has correct service name matching config file

👉 **More troubleshooting:** See [`QUICKSTART.md`](QUICKSTART.md) (Common Issues section)

---

## 📈 Next Steps

### 1. Get It Running
- [ ] Read [QUICKSTART.md](QUICKSTART.md)
- [ ] Start ServiceRegistry
- [ ] Start ConfigServer
- [ ] Start microservices
- [ ] Verify in Eureka dashboard

### 2. Understand It
- [ ] Read [CONFIG_SERVER_ARCHITECTURE.md](CONFIG_SERVER_ARCHITECTURE.md)
- [ ] Review config-repo/ structure
- [ ] Check bootstrap.yml in a service

### 3. Use It
- [ ] Modify a value in config-repo/
- [ ] Test that ConfigServer serves new config
- [ ] Understand configuration priority order
- [ ] Read [CONFIGURATION_GUIDE.md](CONFIGURATION_GUIDE.md)

### 4. Enhance It (Future)
- [ ] Add Git backend instead of filesystem
- [ ] Implement config encryption for secrets
- [ ] Add Spring Cloud Bus for live refreshes
- [ ] Set up external vault for production
- [ ] Create environment-specific profiles

---

## 🎓 Learning Resources

### Spring Cloud Config
- **Official Docs:** https://spring.io/projects/spring-cloud-config/
- **Spring Blog:** https://spring.io/blog
- **Cloud Native Computing Foundation:** https://www.cncf.io/

### Related Technologies
- **Eureka:** Service Discovery
- **Spring Cloud Gateway:** API Gateway
- **Spring Cloud Bus:** Event broadcast
- **Spring Security:** Authentication & Authorization

---

## ❓ FAQ

**Q: Do I need to restart the service after changing a config?**  
A: Not necessarily! If you implement `@RefreshScope` on beans, you can use the `/actuator/refresh` endpoint to reload configs without restart.

**Q: Can I use Git instead of local files?**  
A: Yes! Configure Git backend in ConfigServer's application.yml. Perfect for production.

**Q: What if ConfigServer goes down?**  
A: Services have retry logic. They'll keep running with cached config. When ConfigServer comes back, they reconnect.

**Q: Where do I store secrets (passwords, API keys)?**  
A: Currently in config files (for dev only). For production, use encrypted properties or external vault (HashiCorp Vault, AWS Secrets Manager).

**Q: Can I have different configs per environment?**  
A: Yes! Create `*-dev.yml`, `*-prod.yml` files and use Spring profiles.

**Q: How do I track who changed what in configs?**  
A: Use Git backend (automatic history + version control).

---

## 💡 Pro Tips

1. **Always edit config-repo files**, not service application.yml
2. **Start ConfigServer before other services** - it's required!
3. **Use environment variables for secrets** in production
4. **Keep global configs in application.yml** for reuse
5. **Create profiles early** (dev/staging/prod)
6. **Validate YAML syntax** before committing configs
7. **Document what each property does** in your configs

---

## 🆘 Getting Help

1. **Check [QUICKSTART.md](QUICKSTART.md)** for startup issues
2. **Check [CONFIGURATION_GUIDE.md](CONFIGURATION_GUIDE.md)** for config questions
3. **Check [CONFIG_SERVER_ARCHITECTURE.md](CONFIG_SERVER_ARCHITECTURE.md)** for how it works
4. **Check [MIGRATION_SUMMARY.md](MIGRATION_SUMMARY.md)** for what changed

---

## 📝 Files at a Glance

```
config-repo/
├── application.yml              # Global: management, logging, eureka (all services)
├── authservice.yml              # AuthService: port 8086, JWT, database, feign
├── api-gateway.yml              # API Gateway: port 7070, routes, CORS
├── facilityservice.yml          # FacilityService: port 9099, database, JWT
├── citizenservice.yml           # CitizenService: port 8095, database, JWT
├── emergencyservice.yml         # EmergencyService: port 8083, database, JWT
├── complianceservice.yml        # ComplianceService: port 8084, database
├── patienttreatmentservice.yml  # PatientTreatmentService: port 8082, database, JWT
└── notificationservice.yml      # NotificationService: port 8089, database, email
```

---

## 🎉 Summary

Your CityCare microservices now use **Spring Cloud Config Server**:
- ✅ **Centralized** - All configs in config-repo/
- ✅ **Scalable** - Add services easily without duplication
- ✅ **Production-Ready** - Built-in resilience and retry logic
- ✅ **Standards-Based** - Following Spring Cloud best practices
- ✅ **Future-Proof** - Ready for Git, encryption, vault, etc.

**You're ready to scale! 🚀**

---

## 📞 Version Info

- **Spring Boot:** 3.2.5
- **Spring Cloud:** 2023.0.1
- **Config Server:** Latest from Spring Cloud
- **ConfigServer Module:** Version 1.0.0-SNAPSHOT
- **Date Created:** May 2, 2026

---

**Need something else? Check the docs folder! 📚**

                                           