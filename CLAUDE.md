# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Keycloak SPI (Service Provider Interface) that implements SCIM v2 user provisioning endpoints. The project allows external identity providers (like Entra ID) to manage users in Keycloak via SCIM protocol. The project has been migrated from Java to Kotlin.

## Architecture

- **ScimResourceProvider**: Main JAX-RS resource providing SCIM v2 endpoints (`/realms/{realm}/scim/v2/Users`) - located in `org.example.keycloak.provider`
- **ScimResourceProviderFactory**: Factory for creating ScimResourceProvider instances - located in `org.example.keycloak.provider`
- **Schema classes**: SCIM request/response DTOs in `org.example.keycloak.schemas` (Kotlin data classes)
- **ScimFilterUtil**: Utility for parsing SCIM filter expressions - located in `org.example.keycloak.util`
- **HelloResourceProvider**: Example/test endpoint - located in `org.example.keycloak.provider`
- **JacksonConfig**: JSON serialization configuration - located in `org.example.keycloak.config`

The SPI is registered via `META-INF/services/org.keycloak.services.resource.RealmResourceProviderFactory`.

## Commands

### Build and Package
```bash
# Build JAR (skips tests)
./mvnw clean package -DskipTests

# Build with tests
./mvnw clean package
```

### Testing
```bash
# Run unit tests only (*Test.kt)
./mvnw test

# Run integration tests only (*IT.kt)
./mvnw verify

# Run all tests
./mvnw clean verify
```

### Docker Development
```bash
# Start Keycloak with SPI loaded
docker-compose up --build

# Access Keycloak admin console
# URL: http://localhost:8080
# Username: admin
# Password: password
```

The Docker setup builds the SPI JAR, copies it to Keycloak's providers directory, and runs the `kc.sh build` command to register the provider.

## Testing Environment

Integration tests use Testcontainers with a real Keycloak instance and load test realm configuration from `src/test/resources/test-realm.json`. Tests authenticate via OAuth2 and call SCIM endpoints directly.