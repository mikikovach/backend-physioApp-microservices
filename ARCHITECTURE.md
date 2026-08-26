# Architecture

## Infrastructure
- `service-registry-eureka` (`8761`) — Eureka server. Config: `spring.application.name=SERVICE-REGISTRY`, `eureka.client.register-with-eureka=false`, `eureka.client.fetch-registry=false`.
- `api-gateway` (`9191`) — Spring Cloud Gateway. Routes by Eureka service ID using `lb://AUTH-SERVICE`, `lb://PHYSIO-SERVICE`, `lb://APPOINTMENTS-SERVICE`, `lb://RESERVATIONS-SERVICE`.
- `users-db` (`5433`) — PostgreSQL for `auth-service`, DB `users`.
- `physio-db` (`5434`) — PostgreSQL for `physiotherapists-service`, DB `physios`.
- `slots-db` (`5435`) — PostgreSQL for `appointments-service`, DB `slots`.
- `reservations-db` (`5436`) — PostgreSQL for `reservations-service`, DB `reservations`.
- `frontend` (`4200`) — UI container behind the gateway.
- No Config Server and no message broker (Kafka/RabbitMQ) were found in the scanned scope.

## Services

### `auth-service` (`8081`)
- **Responsibility:** signup/login, JWT issuance, user profile endpoints, admin placeholder endpoint.
- **Base:** PostgreSQL `users`.
- **Main endpoints:** `POST /auth/login`, `POST /auth/signup`, `GET /users/me`, `PUT /users/edit`, `GET /admin/users`.
- **Main dependencies:** `spring-boot-starter-data-jpa`, `spring-boot-starter-webmvc`, `spring-cloud-starter-netflix-eureka-client`, `liquibase-core`, `jjwt-*`, `mapstruct`, `postgresql`.
- **Migrations:** Liquibase (`db/changelog/db.changelog-master.xml`).
- **Key entities:** `User`, `Role` with `User <-> Role` many-to-many via `users_roles`.

### `physiotherapists-service` (`8082`)
- **Responsibility:** physiotherapist read API.
- **Base:** PostgreSQL `physios`.
- **Main endpoints:** `GET /physios`, `GET /physios/{physioId}`.
- **Main dependencies:** `spring-boot-starter-data-jpa`, `spring-boot-starter-webmvc`, `spring-cloud-starter-netflix-eureka-client`, `spring-cloud-starter-circuitbreaker-resilience4j`, `spring-boot-starter-actuator`, `mapstruct`, `postgresql`.
- **Migrations:** SQL init scripts in `init/physio`.
- **Key entity:** `Physiotherapist`.

### `appointments-service` (`8083`)
- **Responsibility:** appointment slot management and reservation/release operations.
- **Base:** PostgreSQL (`slots` in compose; local properties still mention `appointments`).
- **Main endpoints:** `GET /slots/{physioId}`, `GET /slots/admin/{physioId}`, `GET /slots/availability/{slotId}`, `POST /slots/reserve/{slotId}`, `POST /slots/release/{slotId}`, `GET /slots/findSlot/{slotId}`, `POST /slots/insert`.
- **Main dependencies:** `spring-boot-starter-data-jpa`, `spring-boot-starter-webmvc`, `spring-cloud-starter-netflix-eureka-client`, `mapstruct`, `postgresql`.
- **Migrations:** SQL init scripts in `init/appointment-slots`.
- **Key entity:** `AppointmentSlot`.

### `reservations-service` (`8084`)
- **Responsibility:** reservation orchestration; creates/cancels reservations and enriches them with slot + physio data.
- **Base:** PostgreSQL `reservations`.
- **Main endpoints:** `POST /reservations`, `GET /reservations/my-reservations`, `DELETE /reservations/{id}`.
- **Main dependencies:** `spring-boot-starter-data-jpa`, `spring-boot-starter-webmvc`, `spring-boot-starter-webflux`, `spring-cloud-starter-netflix-eureka-client`, `resilience4j-spring-boot3`, `resilience4j-reactor`, `resilience4j-micrometer`, `micrometer-registry-prometheus`, `spring-boot-starter-actuator`, `mapstruct`, `postgresql`.
- **Migrations:** SQL init scripts in `init/reservations`.
- **Key entity:** `Reservation`.

### `service-registry-eureka` (`8761`)
- **Responsibility:** service discovery only.
- **Base:** none.
- **Main dependencies:** `spring-cloud-starter-netflix-eureka-server`.

### `api-bff-service`
- **Out of scope** for this document.

## Communication between services

### Synchronous communication (REST/Feign)
- **No Feign clients** and **no `RestTemplate`** were found.
- `reservations-service` uses `WebClient` for downstream calls:
  - `it.eng.reservations_service.config.SlotServiceClient` -> `http://APPOINTMENTS-SERVICE`
    - `POST /slots/reserve/{slotId}`
    - `GET /slots/findSlot/{slotId}`
    - `POST /slots/release/{slotId}`
  - `it.eng.reservations_service.config.PhysioServiceClient` -> `http://PHYSIO-SERVICE`
    - `GET /physios/{physioId}`
- Gateway routes traffic to:
  - `AUTH-SERVICE` for `/auth/**`, `/users/**`, `/admin/**`
  - `PHYSIO-SERVICE` for `/physios/**`
  - `APPOINTMENTS-SERVICE` for `/slots/**`
  - `RESERVATIONS-SERVICE` for `/reservations/**`

### Asynchronous communication (Message Broker)
- No Kafka/RabbitMQ configuration, producers, consumers, queues, exchanges, or topics were found.
- Communication is synchronous HTTP only.

### Dependency diagram
```text
Client
  -> api-gateway
      -> AUTH-SERVICE
      -> PHYSIO-SERVICE
      -> APPOINTMENTS-SERVICE
      -> RESERVATIONS-SERVICE

reservations-service
  -> APPOINTMENTS-SERVICE
  -> PHYSIO-SERVICE

All services
  -> service-registry-eureka
```

## Authentication and Authorization
- Token enters through `auth-service` (`POST /auth/login`, `POST /auth/signup`) and is returned as JWT.
- `api-gateway` validates Bearer tokens in `MyReactiveJwtAuthFilter` before forwarding requests.
- Gateway adds user context headers: `X-User-email` and `X-User-Id`.
- Downstream services read those headers directly or through `UserContextFilter` in `auth-service`.
- Public paths at the gateway: `/auth/login`, `/auth/signup`, `/physios`, `/slots`.
- Protected paths include `/users/**`, `/admin/**`, `/reservations/**`; `/admin/**` additionally requires `ROLE_ADMIN`.
- No OAuth2 / Spring Security resource-server setup was found.

## Data Layer
- `auth-service`: PostgreSQL `users`; entities `User`, `Role`; many-to-many via `users_roles`; Liquibase migrations.
- `physiotherapists-service`: PostgreSQL `physios`; entity `Physiotherapist`; SQL init scripts.
- `appointments-service`: PostgreSQL `slots`; entity `AppointmentSlot`; SQL init scripts.
- `reservations-service`: PostgreSQL `reservations`; entity `Reservation`; SQL init scripts.
- No shared JPA entities between services were found.
- No service directly accesses another service’s database.

## Cross-Cutting Concerns

### Security
- **Current state:** No standard Spring Security `SecurityConfig`/`SecurityFilterChain` was found in the gateway or downstream services. Authentication is custom: `auth-service` issues a one-hour JWT, Angular stores it in `localStorage`, and its interceptor sends `Authorization: Bearer <token>`. `MyReactiveJwtAuthFilter` in `api-gateway` validates the token, checks `ROLE_ADMIN` for `/admin/**`, and forwards `X-User-Email` and `X-User-Id` to downstream services. `auth-service` additionally uses `UserContextFilter` to require the forwarded email on protected endpoints. BCrypt password hashing is configured.
- **Problem:** JWT validation is concentrated at the gateway. Downstream services do not independently validate tokens, so directly exposed service ports bypass gateway enforcement. The JWT secret is hardcoded in both `auth-service` and `api-gateway`, and `JwtUtil` in the gateway logs that secret in plaintext. Tokens in browser `localStorage` are also exposed to an XSS compromise.
- **Recommendation:** Use Spring Security resource-server support or shared JWT validation in every protected downstream service; restrict service ports to the internal container network. Remove the secret from logs immediately, rotate it, and supply JWT/database credentials through environment variables or a secrets manager. Consider short-lived access tokens with refresh-token rotation and strengthen XSS protections.
- **Priority:** CRITICAL

- **Current state:** Gateway CORS allows only `http://localhost:4200`, with wildcard methods and headers. No CORS configuration was found in downstream services; the frontend Nginx configuration relies on the gateway policy.
- **Problem:** Production frontend origins will fail CORS requests. Wildcard methods and headers are broader than necessary.
- **Recommendation:** Externalize an allowlist of approved frontend origins per environment; explicitly permit only required methods (`GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`) and headers (`Authorization`, `Content-Type`), and configure it at the gateway as the single public entry point.
- **Priority:** HIGH

- **Current state:** `reservations-service` sets `management.endpoints.web.exposure.include=*`, exposing all Actuator endpoints. `physiotherapists-service` exposes only `health,info`; `appointments-service` has no explicit management exposure configuration; `api-gateway` includes Actuator but has no explicit exposure configuration. Gateway JWT filtering protects proxied non-public paths, but direct service access does not have equivalent Spring Security protection.
- **Problem:** Publicly reachable `reservations-service` management endpoints can expose environment, configuration, mappings, metrics, heap/thread diagnostics, or operational details. This is a security risk, especially because the service itself has no independent security layer.
- **Recommendation:** Change `reservations-service` to an explicit minimal allowlist such as `health,info,prometheus`; bind management endpoints to an internal interface or separate management port; and require an operations-only role or network policy for any sensitive endpoint. Explicitly configure and secure management exposure in every service and the gateway.
- **Priority:** CRITICAL

### Input Validation
- **Current state:** Controllers accept request DTOs without `@Valid`/`@Validated`; DTOs do not contain Bean Validation constraints such as `@NotBlank`, `@Email`, `@Size`, or range checks. No custom validators or `spring-boot-starter-validation` dependency were found.
- **Problem:** Invalid or malformed client input reaches service and persistence layers, producing inconsistent errors and increasing the risk of invalid reservations, bad user records, and avoidable server errors.
- **Recommendation:** Add `spring-boot-starter-validation` to each applicable service; add field-level constraints to request DTOs; annotate controller request bodies with `@Valid`; use `@Validated` for path/query constraints; and introduce custom validators where domain rules require them, such as valid appointment times and reservation state transitions. Return a consistent validation-error response from each exception handler.
- **Priority:** HIGH

### Configuration
- **Current state:** Services use local `application.properties` files; no Spring Cloud Config Server was found. `auth-service` has a container profile, but there is no consistent dev/staging/production profile strategy. Angular has development and production builds but no environment files or runtime API configuration. API URLs are hardcoded as `http://localhost:9191`.
- **Problem:** JWT secrets and PostgreSQL credentials are committed in plaintext properties files. Hardcoded frontend URLs and gateway CORS origins prevent portable staging/production deployment. The duplicated/misplaced auth container configuration and Spring Boot/Spring Cloud version skew add configuration drift risk.
- **Recommendation:** Move secrets to environment variables or a secrets manager, rotate exposed credentials, and commit only safe templates. Establish consistent `dev`, `staging`, and `prod` profiles for all services. Externalize frontend API base URLs and CORS origins through Angular environment/runtime configuration. Align Spring Boot and Spring Cloud release trains and remove duplicate configuration files.
- **Priority:** CRITICAL

### Error Handling
- **Current state:** Each service has its own exception handler (`AuthExceptionHandler`, `PhysiosExceptionHandler`, `SlotsExceptionHandler`, `ReservationsExceptionHandler`); the gateway returns separate JWT error responses.
- **Problem:** Error payloads are inconsistent: for example, auth-service returns `errorMessage`, while gateway JWT failures use `error`, `message`, `status`, `timestamp`, and `path`. This forces clients to implement endpoint-specific parsing and makes support, monitoring, and API evolution harder.
- **Recommendation:** Define a shared API error contract, ideally RFC 9457 Problem Details or an equivalent stable schema, containing status, code, message, path, timestamp, correlation ID, and field errors. Apply it in every exception handler and gateway filter; never expose stack traces or sensitive exception details.
- **Priority:** HIGH

### Logging
- **Current state:** SLF4J via Lombok `@Slf4j` is used with default Spring Boot logging and some `DEBUG` log-level properties. The gateway currently logs the JWT secret during token validation.
- **Problem:** Logging secrets is an immediate credential-disclosure vulnerability. DEBUG security logging can disclose authentication details and create noisy production logs. There is no documented structured logging or cross-service request correlation.
- **Recommendation:** Remove secret logging immediately and rotate the disclosed JWT secret. Disable DEBUG security logging outside development, redact tokens/passwords/PII centrally, use structured logs, and propagate a correlation ID from gateway through downstream calls.
- **Priority:** CRITICAL

### Health Checks
- **Current state:** Actuator is enabled in `api-gateway`, `physiotherapists-service`, and `reservations-service`; `reservations-service` exposes all endpoints, while `physiotherapists-service` exposes `health,info`. `appointments-service` relies on default exposure behavior.
- **Problem:** Management exposure is inconsistent, and the reservations configuration exposes significantly more operational data than required. Health checks do not clearly distinguish liveness from readiness or document dependency health.
- **Recommendation:** Standardize `health`, `info`, and, where required, `prometheus` exposure. Configure liveness/readiness health groups, secure management access, and add dependency checks for databases and critical downstream services without exposing sensitive details.
- **Priority:** CRITICAL

### API Documentation
- **Current state:** No Swagger/OpenAPI configuration was found.
- **Problem:** The gateway and independently deployable services lack a machine-readable API contract, making frontend integration, testing, versioning, and error-contract adoption more error-prone.
- **Recommendation:** Add SpringDoc OpenAPI to public API services, document authentication, roles, request validation, responses, and standardized errors, and publish versioned specifications. Restrict interactive Swagger UI outside development or secure it for internal users.
- **Priority:** MEDIUM

### Tracing
- **Current state:** No Zipkin, Sleuth, Jaeger, or OpenTelemetry setup was found. `reservations-service` has Micrometer and Resilience4j metrics.
- **Problem:** A reservation travels through gateway, reservations, appointments, and physiotherapists services without a trace ID, making latency analysis and incident diagnosis difficult.
- **Recommendation:** Add OpenTelemetry tracing at the gateway and all services, propagate W3C trace context through `WebClient`, and export traces to a collector/backend. Correlate trace IDs with structured logs and metrics.
- **Priority:** MEDIUM

### Resilience
- **Current state:** `physiotherapists-service` uses annotation-based `@Retry` and `@CircuitBreaker`; `reservations-service` uses programmatic Resilience4j with `WebClient` operators and retry/circuit settings. Reservations has Micrometer/Prometheus resilience metrics.
- **Problem:** Resilience behavior differs by service, and retries can amplify load or repeat non-idempotent operations such as slot reservation if not carefully bounded. Circuit-breaker fallbacks and user-visible behavior are not documented.
- **Recommendation:** Keep the established style within each service, but define common retry, timeout, circuit-breaker, and fallback standards. Retry only safe/idempotent operations or enforce idempotency keys for reservation creation; add timeouts, test failure scenarios, and alert on circuit state and retry exhaustion.
- **Priority:** MEDIUM

### Frontend-Backend Integration
- **Current state:** Angular's auth interceptor correctly sends `Authorization: Bearer <token>`, matching the gateway JWT filter. Login response fields match the backend response, and frontend route guards mirror authenticated/admin navigation. The gateway derives and forwards user context headers; the frontend does not need to send them.
- **Problem:** Frontend error handling is only partially aligned with backend formats. It handles session-expired `401` responses and one `409` registration case, but most components do not parse gateway `message` or service `errorMessage` payloads consistently. Hardcoded `http://localhost:9191` URLs and localhost-only CORS configuration prevent production deployment.
- **Recommendation:** Introduce a frontend API configuration token/environment or runtime config for the gateway URL. Create one error-normalization layer that understands the standardized backend error contract, maps validation/auth/conflict errors to translated user messages, and preserves errors rather than swallowing them with `EMPTY`. Keep CORS owned by the gateway and deploy frontend/gateway behind controlled origins.
- **Priority:** HIGH

### Summary Table

#### CRITICAL

| Area | Issue | Recommended action |
|:---|:---|:---|
| Security | JWT secret is hardcoded and logged in plaintext | Remove logging, rotate the secret, and use environment variables or a secrets manager. |
| Security / Health checks | `reservations-service` exposes all Actuator endpoints | Use a minimal allowlist and internal or role-protected management access. |
| Configuration | Database credentials and deployment settings are plaintext or hardcoded | Externalize secrets and establish environment profiles. |
| Logging | Security DEBUG logs and secret disclosure risk | Disable production DEBUG logs and centrally redact sensitive values. |

#### HIGH

| Area | Issue | Recommended action |
|:---|:---|:---|
| Security | Downstream services rely on gateway-only authentication | Validate JWTs in services and restrict direct service networking. |
| Input validation | No Bean Validation dependency, constraints, or `@Valid` usage | Add validation dependency, DTO constraints, and consistent error responses. |
| CORS | Localhost-only origin and wildcard methods/headers | Use explicit environment-specific origin, method, and header allowlists. |
| Error handling | Gateway and services return incompatible error payloads | Adopt one versioned error contract. |
| Frontend-backend integration | Hardcoded API URLs and inconsistent error parsing | Add runtime/environment API configuration and shared error normalization. |

#### MEDIUM

| Area | Issue | Recommended action |
|:---|:---|:---|
| API documentation | No OpenAPI/Swagger contract | Add and maintain secured SpringDoc specifications. |
| Tracing | No distributed tracing across reservation flow | Implement OpenTelemetry and trace-context propagation. |
| Resilience | Inconsistent policies and retry risk for state-changing calls | Standardize timeouts, retries, fallbacks, and idempotency controls. |

#### LOW

| Area | Issue | Recommended action |
|:---|:---|:---|
| Observability governance | Management configuration varies by service | Standardize Actuator, health groups, metrics, and alerts. |

## Frontend (Angular)

- **Module structure**
  - Angular 19 app bootstrapped with `bootstrapApplication` in [src/main.ts](src/main.ts) and configured in [src/app/app.config.ts](src/app/app.config.ts).
  - Feature-oriented structure under [src/app/](src/app): `pages`, `services`, `guards`, `models`, `config`.
  - Routing is centralized in [src/app/app.routes.ts](src/app/app.routes.ts), with lazy loading for therapist routes via [src/app/pages/therapists/therapist/therapist.routes.ts](src/app/pages/therapists/therapist/therapist.routes.ts).

- **How it communicates with the backend (services vs Gateway)**
  - Frontend communicates via Angular **services** (`HttpClient`) in [src/app/services/](src/app/services).
  - Calls go directly to hardcoded endpoints on `http://localhost:9191` (e.g. auth, users, physios, slots, reservations).
  - No frontend-side gateway/proxy abstraction is defined; from FE perspective it is direct HTTP to one backend host.

- **Auth flow on the frontend**
  - Login/signup handled in [src/app/services/auth-service/auth.service.ts](src/app/services/auth-service/auth.service.ts); token saved to `localStorage` (`auth_token`).
  - [src/app/guards/auth.interceptor.ts](src/app/guards/auth.interceptor.ts) attaches `Authorization` header when token exists.
  - [src/app/guards/auth.guard.ts](src/app/guards/auth.guard.ts) protects private routes; [src/app/guards/login.guard.ts](src/app/guards/login.guard.ts) redirects already logged-in users away from login.
  - On 401 (except login request), interceptor logs out and redirects to `/login?sessionExpired=true`.
  - Slot-booking flow preserves intended destination via `redirectTarget` in [src/app/services/slot-state.service.ts](src/app/services/slot-state.service.ts).

- **Environment configuration (dev/stage/prod)**
  - No `src/environments` files found; no `fileReplacements` setup.
  - [angular.json](angular.json) defines **development** and **production** build configs.
  - **Stage/staging config is not currently present.**
  - API URLs are currently hardcoded in services (not environment-driven).

- **State management approach**
  - Lightweight local state with Angular **signals** (auth/user/loading/slot state).
  - Async data via `HttpClient` + RxJS Observables in services/components.
  - No NgRx/NGXS/Akita/global store detected.

