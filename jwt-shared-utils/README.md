# jwt-shared-utils

Shared JWT helpers used by CityCare services to keep token parsing and claim extraction consistent.

## What is inside

- `JwtClaimsSupport` for:
  - parsing claims from signed JWT
  - extracting `subject`, `role`, and `userId`
  - token expiration validation

## Try it

Run the module tests from repository root:

```powershell
mvn -pl jwt-shared-utils -am test
```

