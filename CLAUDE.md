# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository layout

Single git repository containing all services as top-level subdirectories. Each service is an independent Maven project with its own `pom.xml` and `mvnw` wrapper.

| Service | Port | Notes |
|---|---|---|
| `service-registry-eureka` | 8761 | Netflix Eureka server |
| `api-gateway` | 9191 | Spring Cloud Gateway, JWT validation, routing |
| `auth-service` | 8081 | JWT issuance, user management, Liquibase migrations |
| `physiotherapists-service` | 8082 | Physio CRUD, Resilience4j (annotation-based) |
| `appointments-service` | 8083 | Appointment slot management |
| `reservations-service` | 8084 | Reservations, calls downstream via WebClient |
| `api-bff-service` | 8085 | BFF (Spring Boot 3.1.4, not in docker-compose) |

## Build and run commands

Run all commands from within the specific service directory.

```bash
# Build (skip tests)
./mvnw clean package -DskipTests

# Run locally
./mvnw spring-boot:run

# Run tests for a service
./mvnw test

# Run a single test class
./mvnw test -Dtest=AuthServiceApplicationTests

# Run a single test method
./mvnw test -Dtest=AuthServiceApplicationTests#authenticate_ShouldReturnUser_WhenCredentialsAreValid
```

## Container / Podman

Each service has a `Containerfile` (Podman-compatible). Build and run pattern:

```bash
# Build image (from service directory, after mvnw package)
podman build -t <service-name>:latest .

# Run all services together (from repo root)
podman-compose -f docker-compose.yml up
```

The `application-container.properties` profile is activated automatically inside containers (`ENV SPRING_PROFILES_ACTIVE=container`) and overrides datasource URLs and Eureka endpoint for the container network.

## Architecture: request flow

```
Frontend (4200) → API Gateway (9191) → Downstream services
```

1. **JWT validation at gateway** — `MyReactiveJwtAuthFilter` (global filter, highest precedence) validates `Authorization: Bearer <token>` for all non-public paths. Public paths: `/auth/login`, `/auth/signup`, `/physios`, `/slots`.
2. **User context propagation** — After validation, the gateway extracts claims and forwards `X-User-Email` and `X-User-Id` headers to downstream services.
3. **UserContextFilter in auth-service** — Reads those headers and stores them as request attributes; returns 401 if absent on non-public paths.

## Authentication (auth-service)

- JWT issued by `JwtService` using JJWT 0.12.5; secret and expiration configured in `application.properties` (`security.jwt.*`).
- The same secret must be present in `api-gateway/application.properties` for token validation.
- Passwords hashed with BCrypt (`ApplicationConfiguration`).
- `User` entity has a `ManyToMany` relationship with `Role` via `users_roles` join table.
- **Liquibase** manages schema in auth-service. Changelogs live in `src/main/resources/db/changelog/` and are referenced by `db.changelog-master.xml`. Other services use plain SQL init scripts under `init/`.

## Resilience patterns

Two styles are used — do not mix them within the same service:

- **Annotation-based** (`physiotherapists-service`): `@CircuitBreaker(name = "physio-service", fallbackMethod = "...")` and `@Retry(name = "physio-service")` on service methods.
- **Programmatic** (`reservations-service`): `ResilienceConfig` registers named `CircuitBreaker` and `Retry` beans from the registry; `SlotServiceClient` / `PhysioServiceClient` apply them manually with reactive operators.

## Service-to-service communication

`reservations-service` calls downstream using `WebClient` with Eureka load-balanced base URLs (`http://APPOINTMENTS-SERVICE`, `http://PHYSIO-SERVICE`). The `WebClient.Builder` is injected and configured in `DownstreamClientsConfig`.

## MapStruct + Lombok annotation processor order

The `maven-compiler-plugin` annotation processor order is **Lombok → MapStruct → lombok-mapstruct-binding** — this order is required. Reversing it breaks code generation.

## Testing conventions

- Prefer `@ExtendWith(MockitoExtension.class)` over `@SpringBootTest` for unit tests (see `PhysioServiceImplTest` vs the older `AuthServiceApplicationTests`).
- `@Mock` + `@InjectMocks` for service-layer tests; no real database or Spring context needed.
- `AuthServiceApplicationTests` mixes a context-load test (`contextLoads`) with plain Mockito tests in a `@SpringBootTest` class — this is legacy; new tests should use the lighter approach.

## Gateway routes

Defined in `api-gateway/src/main/resources/application.properties`:

| Route | Upstream |
|---|---|
| `/auth/**` | AUTH-SERVICE |
| `/users/**` | AUTH-SERVICE |
| `/admin/**` | AUTH-SERVICE |
| `/physios/**` | PHYSIO-SERVICE |
| `/slots/**` | APPOINTMENTS-SERVICE |
| `/reservations/**` | RESERVATIONS-SERVICE |