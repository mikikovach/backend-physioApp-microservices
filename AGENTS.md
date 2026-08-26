# AGENTS.md

## Mission-critical context
- This repo is a **multi-project Maven monorepo**: each top-level folder is an independent Spring Boot service with its own `pom.xml` and `mvnw`.
- Core flow: `Frontend -> api-gateway (9191) -> downstream services`.
- Service discovery and routing use Eureka (`service-registry-eureka`, port `8761`) plus gateway `lb://` routes in `api-gateway/src/main/resources/application.properties`.

## Service boundaries (what lives where)
- `auth-service` (`8081`): login/signup, JWT issuance, users/roles, Liquibase-managed schema.
- `physiotherapists-service` (`8082`): physiotherapist CRUD, annotation-based Resilience4j.
- `appointments-service` (`8083`): appointment slot management.
- `reservations-service` (`8084`): reservation orchestration, reactive WebClient calls to downstream services.
- `api-bff-service` (`8085`): BFF service (not included in `docker-compose.yml`).

## Auth and request context pipeline (critical)
- Gateway validates Bearer tokens globally in `api-gateway/.../MyReactiveJwtAuthFilter.java` (`@Order(HIGHEST_PRECEDENCE)`).
- Public endpoints bypass JWT: `/auth/login`, `/auth/signup`, `/physios`, `/slots`.
- After validation, gateway forwards `X-User-Email` and `X-User-Id` headers to downstream services.
- `auth-service/.../UserContextFilter.java` reads these headers, stores request attributes, and returns `401` on protected paths if missing.
- JWT secret must match between `auth-service` and `api-gateway` properties.

## Build, run, test workflows
- Run commands **inside a specific service directory**.
- Build (skip tests): `./mvnw clean package -DskipTests`
- Run locally: `./mvnw spring-boot:run`
- Service tests: `./mvnw test`
- Single class/method tests:
  - `./mvnw test -Dtest=AuthServiceApplicationTests`
  - `./mvnw test -Dtest=AuthServiceApplicationTests#authenticate_ShouldReturnUser_WhenCredentialsAreValid`

## Containers and local environment
- Container runtime convention is Podman-compatible (`Containerfile` in each service).
- Full stack: `podman-compose -f docker-compose.yml up` from repo root.
- `docker-compose.yml` wires 6 services + 4 PostgreSQL databases on network `mynet`.
- Container profile is auto-enabled via `SPRING_PROFILES_ACTIVE=container`; see `application-container.properties` for container-specific DB/Eureka/JWT values.

## Project-specific coding conventions
- **Do not mix Resilience4j styles inside one service**:
  - `physiotherapists-service`: annotation style (`@CircuitBreaker`, `@Retry`).
  - `reservations-service`: programmatic style via registry + reactive operators.
- `auth-service` database changes go through Liquibase changelogs under `src/main/resources/db/changelog/`; other services use SQL init scripts under `init/`.
- In `auth-service/pom.xml`, annotation processor order is required: **Lombok -> MapStruct -> lombok-mapstruct-binding**.

## Testing patterns used here
- Prefer lightweight unit tests with `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks` for service logic.
- `AuthServiceApplicationTests` is legacy mixed style (`@SpringBootTest` + Mockito); follow newer lightweight tests for new code.

