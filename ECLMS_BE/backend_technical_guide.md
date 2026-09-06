# ECLMS — Enterprise Contract Lifecycle Management System
## Complete Technical Report & Code Walkthrough

---

## Table of Contents

1. [Project Overview & Architecture](#1-project-overview--architecture)
2. [Technology Stack & Dependencies](#2-technology-stack--dependencies)
3. [Complete Workflow — How a Request Flows Through the System](#3-complete-workflow--end-to-end-request-flow)
4. [Backend: Common / Cross-Cutting Layer](#4-backend-common--cross-cutting-layer)
   - 4.1 [Application Entry Point](#41-application-entry-point)
   - 4.2 [Base Entity & Auditing](#42-base-entity--auditing)
   - 4.3 [Configuration Classes](#43-configuration-classes)
   - 4.4 [Security Layer (JWT, Filters, Providers)](#44-security-layer)
   - 4.5 [Exception Handling](#45-exception-handling)
   - 4.6 [API Response Wrapper](#46-api-response-wrapper)
   - 4.7 [Elasticsearch Search Service](#47-elasticsearch-search-service)
   - 4.8 [MinIO / File Storage Service](#48-minio--file-storage-service)
5. [Backend: Feature Modules (Detailed)](#5-backend-feature-modules)
   - 5.1 [Auth Module](#51-auth-module)
   - 5.2 [User Module](#52-user-module)
   - 5.3 [Role Module](#53-role-module)
   - 5.4 [Permission Module](#54-permission-module)
   - 5.5 [Department Module](#55-department-module)
   - 5.6 [Vendor Module](#56-vendor-module)
   - 5.7 [Contract Module](#57-contract-module)
   - 5.8 [Contract Template Module](#58-contract-template-module)
   - 5.9 [Document Module](#59-document-module)
   - 5.10 [Workflow Module](#510-workflow-module)
   - 5.11 [Dashboard Module](#511-dashboard-module)
   - 5.12 [Report Module](#512-report-module)
6. [Frontend (Angular): Architecture & Components](#6-frontend-angular)
7. [Database Schema & Migrations](#7-database-schema--migrations)
8. [Annotations & Decorators Reference Guide](#8-annotations--decorators-reference-guide)

---

## 1. Project Overview & Architecture

**ECLMS** (Enterprise Contract Lifecycle Management System) is a full-stack enterprise application that manages the entire lifecycle of business contracts — from creation through approval workflows to renewal and termination. It is built as a monorepo containing:

| Layer | Technology | Directory |
|-------|-----------|-----------|
| **Backend API** | Spring Boot 3.3.4 (Java 17) | `src/main/java/com/company/eclms/` |
| **Frontend SPA** | Angular 19 (Standalone Components) | `ECLMS_FE/src/app/` |
| **Database** | MySQL 8+ with Flyway migrations | `src/main/resources/db/migration/` |
| **Cache & Sessions** | Redis | External service |
| **Full-Text Search** | Elasticsearch | External service |
| **Object Storage** | MinIO (S3-compatible) | External service |
| **Message Broker** | RabbitMQ | External service (dependency declared) |

### Architectural Pattern

The backend follows a **Modular Layered Architecture**:

```
┌─────────────────────────────────────────────────────────────┐
│                     Angular Frontend (SPA)                  │
│    Guards → Interceptors → Services → Components → Views    │
├─────────────────────────────────────────────────────────────┤
│                        HTTP / REST API                      │
├─────────────────────────────────────────────────────────────┤
│  Rate Limiting Filter → JWT Auth Filter → Security Context  │
├─────────────────────────────────────────────────────────────┤
│              REST Controllers (@RestController)             │
├─────────────────────────────────────────────────────────────┤
│              Service Layer (@Service / Interface + Impl)    │
├─────────────────────────────────────────────────────────────┤
│              MapStruct Mappers (@Mapper)                    │
├─────────────────────────────────────────────────────────────┤
│              Repository Layer (Spring Data JPA)             │
├─────────────────────────────────────────────────────────────┤
│   MySQL (Primary)  │  Redis (Cache)  │  Elasticsearch (FTS)│
│   MinIO (Files)    │  RabbitMQ (MQ)  │                     │
└─────────────────────────────────────────────────────────────┘
```

Each business module (e.g., `contract`, `user`, `vendor`) follows the same internal package structure:
- `controller/` — REST endpoints
- `dto/` — Data Transfer Objects (request/response payloads)
- `entity/` — JPA entities (database table mappings)
- `mapper/` — MapStruct interfaces for Entity ↔ DTO conversion
- `repository/` — Spring Data JPA repository interfaces
- `service/` — Business logic interface
- `service/impl/` — Business logic implementation

---

## 2. Technology Stack & Dependencies

### Build System

| Artifact | Value |
|----------|-------|
| Build Tool | Maven (via `pom.xml`) |
| Java Version | 17 |
| Spring Boot | 3.3.4 |
| Group ID | `com.company` |
| Artifact ID | `eclms` |

### Key Dependencies Explained

| Dependency | Purpose | Why Used |
|-----------|---------|----------|
| `spring-boot-starter-web` | Embeds Tomcat, provides `@RestController`, Jackson JSON serialization | Core of the REST API layer |
| `spring-boot-starter-security` | Authentication & authorization framework | Protects all endpoints; provides filter chain, `AuthenticationManager` |
| `spring-boot-starter-validation` | Bean Validation (Jakarta Validation / Hibernate Validator) | Enables `@NotBlank`, `@Valid`, `@Size` annotations for DTO validation |
| `spring-boot-starter-actuator` | Health checks and operational monitoring | `/actuator` endpoints for DevOps visibility |
| `spring-boot-starter-aop` | Aspect-Oriented Programming support | Enables cross-cutting concerns (logging, security annotations) |
| `spring-boot-starter-data-jpa` | JPA + Hibernate ORM integration | Maps Java objects to database tables; provides repositories |
| `spring-boot-starter-data-redis` | Redis client (Lettuce-based) | Caches refresh tokens, OTPs, rate-limiting counters, and blacklisted JWTs |
| `spring-boot-starter-data-elasticsearch` | Elasticsearch client | Full-text search across contracts (name, content, vendor, OCR text) |
| `spring-boot-starter-amqp` | RabbitMQ integration | Declared for asynchronous messaging (notifications, audit events) |
| `mysql-connector-j` | MySQL JDBC driver | Connects JPA/Hibernate to MySQL database |
| `h2` | In-memory database for testing | Used for H2 profile-based local testing |
| `flyway-core` + `flyway-mysql` | Database migration tool | Version-controls the DB schema with SQL migration scripts |
| `jjwt-api` / `jjwt-impl` / `jjwt-jackson` | JJWT library (JSON Web Token) | Creates and validates JWT access & refresh tokens |
| `minio` | MinIO Java SDK (S3-compatible) | Uploads, downloads, and manages contract documents in object storage |
| `springdoc-openapi-starter-webmvc-ui` | Swagger UI + OpenAPI 3 docs | Auto-generates interactive API documentation at `/swagger-ui.html` |
| `poi-ooxml` | Apache POI (Excel generation) | Exports contract reports as `.xlsx` spreadsheets |
| `openpdf` | PDF generation library (LibrePDF) | Exports contract reports as `.pdf` documents |
| `lombok` | Compile-time code generation | Eliminates boilerplate: getters, setters, constructors, builders, loggers |
| `mapstruct` | Compile-time bean mapper | Generates type-safe Entity ↔ DTO mapping code at build time |
| `lombok-mapstruct-binding` | Lombok + MapStruct interop | Ensures Lombok generates code before MapStruct processes it |
| `spring-boot-starter-test` | JUnit 5 + Mockito + Spring Test | Unit and integration testing |
| `spring-security-test` | Security-specific test utilities | Testing secured endpoints with mock authentication |
| `testcontainers` | Docker-based integration testing | Spins up real PostgreSQL/MySQL containers for integration tests |

### Annotation Processor Order (Build Plugin)

The `maven-compiler-plugin` is configured with a specific annotation processor order:
1. **Lombok** runs first → generates getters, setters, constructors
2. **MapStruct** runs second → reads Lombok-generated methods to create mapper implementations
3. **Lombok-MapStruct Binding** → bridges the two so MapStruct sees Lombok's output

---

## 3. Complete Workflow — End-to-End Request Flow

### 3.1 Login & Authentication Flow

```
User (Browser) → POST /api/v1/auth/login { username, password }
    │
    ├─► RateLimitingFilter.doFilterInternal()
    │       Checks Redis for rate-limit counter (key = "rate_limit:{user}:{ip}")
    │       If exceeded → 429 Too Many Requests
    │       If Redis down → falls back to in-memory ConcurrentHashMap
    │
    ├─► JwtAuthenticationFilter.doFilterInternal()
    │       No "Authorization: Bearer ..." header → skip (unauthenticated)
    │
    ├─► SecurityFilterChain (SecurityConfig)
    │       "/api/v1/auth/**" is permitAll → request reaches controller
    │
    ├─► AuthController.login(LoginRequest, HttpServletRequest)
    │       Extracts IP address and User-Agent from request
    │       Delegates to AuthService.login()
    │
    ├─► AuthServiceImpl.login()
    │   ├── Finds User by username in DB
    │   ├── Checks if account is locked (failed attempts ≥ 5 → locked for 15 min)
    │   ├── Calls AuthenticationManager.authenticate() with credentials
    │   │     └── DaoAuthenticationProvider → CustomUserDetailsService.loadUserByUsername()
    │   │           └── Queries UserRepository → builds CustomUserDetails with roles & permissions
    │   │           └── BCryptPasswordEncoder verifies password hash
    │   ├── On success:
    │   │     ├── Generates access token (15 min) via JwtTokenProvider
    │   │     ├── Generates refresh token (7 days) via JwtTokenProvider
    │   │     ├── Stores refresh token in Redis ("refresh:{username}")
    │   │     ├── Resets failedLoginAttempts to 0
    │   │     ├── Logs login history to login_histories table
    │   │     └── Returns LoginResponse { accessToken, refreshToken, UserDto }
    │   └── On failure:
    │         ├── Increments failedLoginAttempts
    │         ├── If attempts ≥ 5 → locks account
    │         ├── Logs failed attempt to login_histories
    │         └── Throws UnauthorizedException
    │
    └─► Response: ApiResponse<LoginResponse> { success: true, data: { accessToken, refreshToken, user } }
```

### 3.2 Authenticated API Request Flow

```
User (Browser) → GET /api/v1/contracts?page=0&size=10
    Headers: { Authorization: "Bearer eyJhbGciOi..." }
    │
    ├─► RateLimitingFilter
    │       Checks rate limit for this user+IP
    │
    ├─► JwtAuthenticationFilter.doFilterInternal()
    │   ├── parseJwt() → extracts token from "Bearer " header
    │   ├── tokenProvider.isTokenExpired(jwt) → checks expiration claim
    │   ├── tokenProvider.extractUsername(jwt) → extracts subject claim
    │   ├── userDetailsService.loadUserByUsername(username) → loads user + roles + permissions
    │   ├── tokenProvider.validateToken(jwt, userDetails) → matches username & expiry
    │   ├── Creates UsernamePasswordAuthenticationToken with authorities
    │   └── Sets authentication in SecurityContextHolder (thread-local)
    │
    ├─► SecurityFilterChain
    │       anyRequest().authenticated() → authentication exists → passes
    │
    ├─► @PreAuthorize (on controller method)
    │       Checks if user has required permission (e.g., "CONTRACT_READ")
    │
    ├─► ContractController.getContracts(pageable, search, status, ...)
    │       Delegates to ContractService.getContracts()
    │
    ├─► ContractServiceImpl.getContracts()
    │   ├── Builds JPA Specification dynamically (search + status + departmentId + vendorId)
    │   ├── Calls contractRepository.findAll(spec, pageable)
    │   │     └── Hibernate generates SQL: SELECT ... WHERE conditions ... LIMIT ... OFFSET ...
    │   ├── Maps Page<Contract> → Page<ContractDto> via ContractMapper
    │   └── Returns paginated DTO result
    │
    └─► Response: ApiResponse<Page<ContractDto>> wrapped by controller
```

### 3.3 Contract Lifecycle Workflow

```
                ┌──────────┐
                │  CREATE   │  (ContractService.createContract)
                │  DRAFT    │  Status = "DRAFT", Version = "1.0"
                └────┬─────┘
                     │
                     ▼
         ┌───────────────────────┐
         │  START WORKFLOW       │  (WorkflowService.startWorkflow)
         │  Contract → IN_REVIEW │  Creates WorkflowInstance
         └──────────┬────────────┘
                    │
            ┌───────┴───────┐
            ▼               ▼
     ┌──────────┐    ┌──────────┐
     │  Step 1   │    │ REJECTED │  (WorkflowService.approveOrRejectStep)
     │  USER     │────│ Contract │  Status → "REJECTED"
     │  APPROVAL │    │ goes back│  Can be edited and re-submitted
     └────┬─────┘    └──────────┘
          │ APPROVED
          ▼
     ┌──────────┐
     │  Step 2   │
     │  ADMIN    │────► REJECTED (same as above)
     │  APPROVAL │
     └────┬─────┘
          │ APPROVED (Final step)
          ▼
     ┌──────────┐
     │  ACTIVE   │  Contract is now active and enforceable
     │           │  Status = "ACTIVE"
     └────┬─────┘
          │
    ┌─────┴──────┐
    ▼            ▼
┌────────┐  ┌──────────┐
│ RENEW  │  │TERMINATE │  (ContractService.terminateContract)
│ Version│  │Status =  │  
│ +1.0   │  │TERMINATED│
│ → DRAFT│  └──────────┘
└────────┘
```

---

## 4. Backend: Common / Cross-Cutting Layer

### 4.1 Application Entry Point

**File**: [EclmsApplication.java](file:///c:/Users/gagan/Downloads/New%20folder(3)/New%20folder/src/main/java/com/company/eclms/EclmsApplication.java)

```java
@SpringBootApplication
public class EclmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(EclmsApplication.class, args);
    }
}
```

| Element | Explanation |
|---------|-------------|
| `@SpringBootApplication` | **Meta-annotation** combining three annotations: `@Configuration` (this class is a source of bean definitions), `@EnableAutoConfiguration` (tells Spring Boot to automatically configure beans based on classpath dependencies — e.g., sees `spring-boot-starter-web` → configures embedded Tomcat, sees `spring-boot-starter-data-jpa` → configures Hibernate), and `@ComponentScan` (scans `com.company.eclms` and all sub-packages for `@Component`, `@Service`, `@Repository`, `@Controller` classes). |
| `SpringApplication.run()` | **Bootstrap method** — creates the Spring `ApplicationContext`, starts the embedded Tomcat server, triggers all `@PostConstruct` methods, runs `CommandLineRunner` beans (like `DataInitializer`), and starts listening for HTTP requests. |
| `main(String[] args)` | Standard Java entry point. `args` are passed to Spring Boot for parsing command-line overrides (e.g., `--server.port=9090`). |

---

### 4.2 Base Entity & Auditing

**File**: [BaseEntity.java](file:///c:/Users/gagan/Downloads/New%20folder(3)/New%20folder/src/main/java/com/company/eclms/common/entity/BaseEntity.java)

This abstract class provides common fields inherited by **every** entity in the system.

| Field | Annotation | Purpose |
|-------|-----------|---------|
| `id` (UUID) | `@Id`, `@GeneratedValue(strategy = AUTO)` | Primary key. `@Id` marks it as the JPA identifier. `GenerationType.AUTO` lets Hibernate choose the generation strategy (UUID generator for UUID types). `@Column(updatable = false, nullable = false)` prevents accidental ID modification. |
| `createdAt` | `@CreatedDate` | Automatically populated by Spring Data JPA auditing with the timestamp when the entity is first persisted. `updatable = false` ensures it never changes after creation. |
| `updatedAt` | `@LastModifiedDate` | Automatically updated to current timestamp every time the entity is modified. |
| `createdBy` | `@CreatedBy` | Automatically populated with the username of the authenticated user (from `SpringSecurityAuditorAware`). |
| `updatedBy` | `@LastModifiedBy` | Automatically updated with the current user on each modification. |
| `deleted` | — | Soft-delete flag. When `true`, the record is considered "deleted" but remains in the database for audit purposes. Default `false`. |
| `deletedAt` / `deletedBy` | — | Track when and by whom a soft-delete was performed. |
| `version` | `@Version` | **Optimistic locking** field. JPA increments this automatically on each update. If two concurrent requests try to update the same record, the second one gets an `OptimisticLockException`. This prevents lost updates in concurrent environments. |

**Key Annotations on the Class:**

| Annotation | Purpose |
|-----------|---------|
| `@Getter` / `@Setter` | **Lombok** — generates all getter and setter methods at compile time, eliminating ~30 lines of boilerplate. |
| `@MappedSuperclass` | **JPA** — tells Hibernate that this class is NOT a standalone entity (no `base_entity` table is created). Instead, its fields are inherited and mapped into the tables of subclasses (`contracts`, `users`, etc.). |
| `@EntityListeners(AuditingEntityListener.class)` | **Spring Data JPA** — registers the auditing listener that populates `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy` fields automatically. |

**File**: [SpringSecurityAuditorAware.java](file:///c:/Users/gagan/Downloads/New%20folder(3)/New%20folder/src/main/java/com/company/eclms/common/audit/SpringSecurityAuditorAware.java)

```java
@Component
public class SpringSecurityAuditorAware implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.of("SYSTEM");
        }
        return Optional.of(authentication.getName());
    }
}
```

| Element | Explanation |
|---------|-------------|
| `implements AuditorAware<String>` | Spring Data interface — returns the current "auditor" (username) for `@CreatedBy`/`@LastModifiedBy` fields. Generic type `<String>` matches the field type. |
| `SecurityContextHolder.getContext().getAuthentication()` | Retrieves the authentication object from the thread-local security context (set by `JwtAuthenticationFilter`). |
| Returns `"SYSTEM"` for anonymous/unauthenticated | During startup (e.g., `DataInitializer` seeding data), there's no authenticated user. Returns `"SYSTEM"` so `createdBy` is never null. |
| `@Component` | Registers this as a Spring-managed bean so it can be referenced by `JpaAuditingConfig`. |

**File**: [JpaAuditingConfig.java](file:///c:/Users/gagan/Downloads/New%20folder(3)/New%20folder/src/main/java/com/company/eclms/common/config/JpaAuditingConfig.java)

```java
@Configuration
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
public class JpaAuditingConfig { }
```

| Annotation | Purpose |
|-----------|---------|
| `@Configuration` | Marks this as a configuration class (a source of `@Bean` definitions). |
| `@EnableJpaAuditing` | Activates Spring Data JPA's auditing feature. `auditorAwareRef` points to the bean name of `SpringSecurityAuditorAware` (Spring auto-generates the name from the class name in camelCase). |

---

### 4.3 Configuration Classes

#### SecurityConfig

**File**: [SecurityConfig.java](file:///c:/Users/gagan/Downloads/New%20folder(3)/New%20folder/src/main/java/com/company/eclms/common/config/SecurityConfig.java)

This is the **heart of the security configuration**. It defines:

| Method / Bean | What It Does |
|--------------|--------------|
| `passwordEncoder()` | Returns `BCryptPasswordEncoder` — hashes passwords with bcrypt (10 rounds by default). Used for both storing and verifying passwords. BCrypt includes a random salt in each hash, making rainbow-table attacks infeasible. |
| `authenticationProvider()` | Creates a `DaoAuthenticationProvider` that uses `CustomUserDetailsService` to load users from DB and `BCryptPasswordEncoder` to verify passwords. This is the bridge between Spring Security and your user database. |
| `authenticationManager()` | Exposes the `AuthenticationManager` as a bean. This is needed by `AuthServiceImpl` to programmatically authenticate login requests via `authenticationManager.authenticate()`. |
| `filterChain(HttpSecurity)` | **The main security filter chain**: |

**Filter Chain Details:**

| Configuration | Purpose |
|--------------|---------|
| `.csrf(disable)` | Disables CSRF protection because this is a stateless JWT API (no cookies/sessions = no CSRF risk). |
| `.cors(corsConfigurationSource())` | Enables CORS with the custom source — allows Angular frontend on a different port to call the API. |
| `.exceptionHandling(entryPoint, deniedHandler)` | Sets custom JSON error responses for 401 (unauthenticated) and 403 (forbidden) instead of Spring's default HTML error pages. |
| `.sessionManagement(STATELESS)` | Tells Spring Security to **never** create HTTP sessions. Every request must carry its own JWT token. |
| `.authorizeHttpRequests()` | Configures URL-level authorization: `/api/v1/auth/**`, `/swagger-ui/**`, `/actuator/**` are public; everything else requires authentication. |
| `addFilterBefore(jwtAuth, UsernamePasswordAuth)` | Inserts the JWT validation filter **before** Spring's default form-login filter. |
| `addFilterBefore(rateLimit, JwtAuth)` | Inserts rate limiting **before** JWT auth (so rate-limited requests don't even get authenticated). |

**Key Annotations:**

| Annotation | Purpose |
|-----------|---------|
| `@EnableWebSecurity` | Enables Spring Security's web MVC integration and auto-configuration. |
| `@EnableMethodSecurity` | Enables `@PreAuthorize` and `@PostAuthorize` annotations on individual controller methods for fine-grained authorization. |
| `@RequiredArgsConstructor` | **Lombok** — generates a constructor with all `final` fields as parameters. Spring uses constructor injection to wire in all the dependencies. |

**CORS Configuration:**

```java
configuration.setAllowedOriginPatterns(List.of("*"));    // Any origin (dev mode)
configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", ...));
configuration.setAllowCredentials(true);
```

This allows the Angular frontend (running on `localhost:4200`) to call the backend (on `localhost:8082`).

#### OpenApiConfig

**File**: [OpenApiConfig.java](file:///c:/Users/gagan/Downloads/New%20folder(3)/New%20folder/src/main/java/com/company/eclms/common/config/OpenApiConfig.java)

Configures Swagger/OpenAPI documentation:

| Element | Purpose |
|---------|---------|
| `customOpenAPI()` | Creates an `OpenAPI` bean with API title, version, and description. |
| `SecurityScheme("bearerAuth")` | Adds a "Bearer JWT" authentication scheme to Swagger UI so you can test authenticated endpoints directly from the docs by entering your JWT token. |
| `SecurityRequirement` | Applies the bearer auth requirement globally to all endpoints. |

#### DataInitializer

**File**: [DataInitializer.java](file:///c:/Users/gagan/Downloads/New%20folder(3)/New%20folder/src/main/java/com/company/eclms/common/config/DataInitializer.java)

| Element | Explanation |
|---------|-------------|
| `implements CommandLineRunner` | Spring Boot interface — `run()` is called automatically after application context is loaded. Used here to seed initial data. |
| `@Transactional` | Wraps the entire `run()` method in a single database transaction. If any part fails, all changes roll back. |
| `@Slf4j` | **Lombok** — generates a `private static final Logger log = LoggerFactory.getLogger(DataInitializer.class)` field. |
| **Seeds Permissions** | Creates 43 permissions (USER_READ, CONTRACT_CREATE, etc.) with `findByName().orElseGet(() -> save())` — only creates if not already present (idempotent). |
| **Seeds Roles** | Creates ADMIN and USER roles with all permissions attached. |
| **Seeds Admin User** | Creates or resets the default admin user (username: `admin`, password: `admin123`). Password is BCrypt-encoded. |
| **Seeds Workflows** | Creates 4 default two-step sequential approval workflows (Vendor Approval, NDA, Software License, Consulting Agreement). Each has Step 1 (USER role approval) and Step 2 (ADMIN role approval). |

---

### 4.4 Security Layer

#### JwtTokenProvider

**File**: [JwtTokenProvider.java](file:///c:/Users/gagan/Downloads/New%20folder(3)/New%20folder/src/main/java/com/company/eclms/common/security/JwtTokenProvider.java)

| Method | Purpose |
|--------|---------|
| `@PostConstruct init()` | Called once after bean construction. Decodes the JWT secret (Base64 or raw bytes) and creates an HMAC-SHA key. `@PostConstruct` ensures the key is ready before any token operations. |
| `generateAccessToken(UserDetails)` | Creates a JWT with: subject = username, roles claim = list of authorities, issued-at = now, expiry = now + 15 min. Signs with HMAC-SHA. |
| `generateRefreshToken(UserDetails)` | Creates a JWT with just the subject (no roles), expiry = now + 7 days. |
| `createToken(claims, subject, expirationMs)` | Private helper — builds the JWT using `Jwts.builder()`, sets claims, subject, dates, signs with key, and compacts to a string. |
| `extractUsername(token)` | Parses the JWT and extracts the `sub` (subject) claim. |
| `extractClaim(token, resolver)` | Generic claim extractor using a `Function<Claims, T>` — functional programming pattern for flexible claim extraction. |
| `extractAllClaims(token)` | Parses and verifies the JWT signature. If invalid, throws `JwtException`. |
| `isTokenExpired(token)` | Returns `true` if the token's expiration date is in the past. Catches exceptions and returns `true` (treat invalid tokens as expired). |
| `validateToken(token, userDetails)` | Validates both: (1) username in token matches the loaded user, and (2) token is not expired. |

**Key annotations:**

| Annotation | Purpose |
|-----------|---------|
| `@Value("${app.security.jwt.secret}")` | Injects the JWT secret from `application.properties`. The `${...}` syntax reads from Spring's property sources. |
| `@PostConstruct` | JPA/Spring lifecycle — method runs exactly once after dependency injection, used for initialization that depends on injected values. |
| `@Component` | Registers this class as a Spring bean, making it injectable via constructor injection. |

#### JwtAuthenticationFilter

**File**: [JwtAuthenticationFilter.java](file:///c:/Users/gagan/Downloads/New%20folder(3)/New%20folder/src/main/java/com/company/eclms/common/security/JwtAuthenticationFilter.java)

| Element | Explanation |
|---------|-------------|
| `extends OncePerRequestFilter` | Spring Security abstract class — guarantees `doFilterInternal()` is called exactly once per request, even with forwards/includes. |
| `doFilterInternal()` | Core logic: extracts JWT from `Authorization` header → validates → loads user from DB → sets `SecurityContext`. |
| `parseJwt(request)` | Extracts the raw JWT from the `Authorization: Bearer <token>` header. Returns `null` if header is missing or malformed. |
| `UsernamePasswordAuthenticationToken` | Spring Security authentication object. Created with `(userDetails, null, authorities)` — the `null` credential (password) is fine since we already validated via JWT. |
| `WebAuthenticationDetailsSource` | Attaches request details (IP address, session ID) to the authentication object for audit logging. |
| `SecurityContextHolder.getContext().setAuthentication()` | Sets the authentication on the thread-local security context. All downstream code (`@PreAuthorize`, `SpringSecurityAuditorAware`, etc.) reads from this context. |

#### RateLimitingFilter

**File**: [RateLimitingFilter.java](file:///c:/Users/gagan/Downloads/New%20folder(3)/New%20folder/src/main/java/com/company/eclms/common/security/RateLimitingFilter.java)

Implements a **fixed-window rate limiter** with Redis (primary) and in-memory (fallback):

| Method | Purpose |
|--------|---------|
| `resolveRateLimitKey()` | Creates a unique key per user+IP: `"rate_limit:{username}:{ip}"`. Unauthenticated users get `"ANONYMOUS"`. |
| `isAllowed(key)` | **Redis path**: Reads the counter from Redis. If null (first request in window), sets to "1" with 1-minute TTL. If < limit, increments. If ≥ limit, rejects. **Fallback path**: If Redis throws any exception, uses an in-memory `ConcurrentHashMap<String, RequestBucket>`. Each bucket tracks count and window start time. Resets when 60 seconds have passed. |
| `RequestBucket` | Inner class with `AtomicInteger` (thread-safe counter) and `AtomicLong` (window timestamp). |
| Default limit | 60 requests per minute (configurable via `app.rate-limit.requests-per-minute`). |

#### CustomUserDetails & CustomUserDetailsService

**File**: [CustomUserDetails.java](file:///c:/Users/gagan/Downloads/New%20folder(3)/New%20folder/src/main/java/com/company/eclms/common/security/CustomUserDetails.java)

| Element | Explanation |
|---------|-------------|
| `implements UserDetails` | Spring Security interface — provides user information to the authentication framework. |
| `id`, `username`, `email`, `password`, `enabled`, `authorities` | Custom fields. The `@Builder` pattern allows fluent construction. |
| `isAccountNonExpired()` etc. | All return `true` — account expiry/locking is handled in `AuthServiceImpl` instead. The `isEnabled()` method delegates to the `enabled` field which is computed from `user.status == "ACTIVE" && !user.isLocked()`. |

**File**: [CustomUserDetailsService.java](file:///c:/Users/gagan/Downloads/New%20folder(3)/New%20folder/src/main/java/com/company/eclms/common/security/CustomUserDetailsService.java)

| Element | Explanation |
|---------|-------------|
| `implements UserDetailsService` | Spring Security SPI — the `loadUserByUsername()` method is called by `DaoAuthenticationProvider` during login authentication. |
| `@Transactional(readOnly = true)` | Optimization — tells Hibernate to skip dirty-checking on loaded entities, and tells the JDBC driver to hint the DB that no writes will occur. |
| **Authority building** | Iterates over user's roles → for each role, adds `ROLE_ADMIN` authority (Spring Security convention for role-based checks) AND each permission's name (e.g., `CONTRACT_READ`) for fine-grained `@PreAuthorize` checks. |

#### JwtAuthenticationEntryPoint & JwtAccessDeniedHandler

These two classes handle authentication/authorization errors by returning **structured JSON** responses instead of Spring's default HTML error pages.

| Class | HTTP Status | When Triggered |
|-------|-------------|----------------|
| `JwtAuthenticationEntryPoint` | 401 Unauthorized | Request to protected endpoint with no/invalid JWT |
| `JwtAccessDeniedHandler` | 403 Forbidden | Valid JWT but insufficient permissions |

Both use `ObjectMapper` with `JavaTimeModule` to properly serialize `LocalDateTime` in the `ApiResponse`.

---

### 4.5 Exception Handling

**File**: [GlobalExceptionHandler.java](file:///c:/Users/gagan/Downloads/New%20folder(3)/New%20folder/src/main/java/com/company/eclms/common/exception/GlobalExceptionHandler.java)

| Annotation | Purpose |
|-----------|---------|
| `@RestControllerAdvice` | Combines `@ControllerAdvice` (applies to all controllers) and `@ResponseBody` (return values are serialized to JSON). This class acts as a global exception interceptor. |
| `@ExceptionHandler(ExceptionType.class)` | Each method handles a specific exception type. Spring matches the most specific handler first. |

| Exception | HTTP Status | Scenario |
|-----------|-------------|----------|
| `BusinessException` | Dynamic (from exception) | Generic business rule violation |
| `MethodArgumentNotValidException` | 400 Bad Request | `@Valid` validation fails on a DTO (e.g., `@NotBlank` fields are empty) |
| `AccessDeniedException` | 403 Forbidden | `@PreAuthorize` check fails |
| `BadCredentialsException` | 401 Unauthorized | Wrong username/password during login |
| `Exception` (catch-all) | 500 Internal Server Error | Any unhandled exception |

**Custom Exception Hierarchy:**

| Exception | Extends | Status | Usage |
|-----------|---------|--------|-------|
| `BusinessException` | `RuntimeException` | Configurable | Base for business errors with HTTP status |
| `NotFoundException` | `BusinessException` | 404 | Entity not found (contract, user, etc.) |
| `ConflictException` | `BusinessException` | 409 | State conflict (e.g., updating active contract) |
| `UnauthorizedException` | `BusinessException` | 401 | Invalid credentials, expired token |
| `ForbiddenException` | `BusinessException` | 403 | Insufficient permissions |
| `ValidationException` | `BusinessException` | 400 | Custom validation failures |
| `StorageException` | `BusinessException` | 500 | MinIO/file storage errors |
| `DocumentException` | `BusinessException` | 500 | Document processing errors |
| `WorkflowException` | `BusinessException` | 400 | Workflow state machine violations |
| `VendorException` | `BusinessException` | 400 | Vendor-related errors |
| `AIException` | `BusinessException` | 500 | AI/ML integration errors |

---

### 4.6 API Response Wrapper

**File**: [ApiResponse.java](file:///c:/Users/gagan/Downloads/New%20folder(3)/New%20folder/src/main/java/com/company/eclms/common/response/ApiResponse.java)

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private List<String> errors;
    private LocalDateTime timestamp;
}
```

| Annotation | Purpose |
|-----------|---------|
| `@Data` | **Lombok** — generates `@Getter`, `@Setter`, `@ToString`, `@EqualsAndHashCode`, and `@RequiredArgsConstructor` all at once. |
| `@Builder` | **Lombok** — generates a Builder pattern (`ApiResponse.builder().success(true).data(dto).build()`). |
| `@NoArgsConstructor` | Required by Jackson for JSON deserialization. |
| `@AllArgsConstructor` | Required by `@Builder` to work alongside `@NoArgsConstructor`. |

**Factory Methods:**

| Method | Purpose |
|--------|---------|
| `success(T data, String message)` | Creates a success response with `success=true`, a message, and the data payload. |
| `success(T data)` | Overload with default message "Operation completed successfully". |
| `error(List<String> errors, String message)` | Creates an error response with `success=false` and a list of error messages. |
| `error(String error, String message)` | Convenience overload wrapping a single error string into a list. |

**Why use a generic wrapper?** Every API endpoint returns the same structure, so the frontend can consistently check `response.success` and access `response.data` or `response.errors` regardless of the endpoint.

---

### 4.7 Elasticsearch Search Service

**File**: [ContractSearchService.java](file:///c:/Users/gagan/Downloads/New%20folder(3)/New%20folder/src/main/java/com/company/eclms/common/search/ContractSearchService.java) (Interface)

**File**: [ContractSearchServiceImpl.java](file:///c:/Users/gagan/Downloads/New%20folder(3)/New%20folder/src/main/java/com/company/eclms/common/search/ContractSearchServiceImpl.java) (Implementation)

| Method | Purpose |
|--------|---------|
| `indexContract(id, name, content, vendorName, status, ocrText)` | Indexes a contract document in Elasticsearch. Sends a PUT to `/contracts/_doc/{id}` with a JSON body containing all searchable fields. If Elasticsearch is down, logs a warning and continues (graceful degradation). |
| `deleteIndex(contractId)` | Removes a contract from the Elasticsearch index when soft-deleted. |
| `searchContractIds(searchTerm)` | Executes a **multi_match** query across `name` (boost ×3), `content`, `vendorName` (boost ×2), and `ocrText`. Returns a list of matching contract UUIDs. The boosting ensures name matches rank higher than content matches. |

**Implementation Details:**
- Uses `RestClient` (Spring 6+ HTTP client) instead of the high-level Elasticsearch Java client for simplicity.
- Lazy initialization of `RestClient` via `getClient()` method.
- All operations wrapped in try-catch — Elasticsearch is treated as an **optional** service.

---

### 4.8 MinIO / File Storage Service

**File**: [StorageService.java](file:///c:/Users/gagan/Downloads/New%20folder(3)/New%20folder/src/main/java/com/company/eclms/common/storage/StorageService.java) (Interface)

**File**: [MinioStorageService.java](file:///c:/Users/gagan/Downloads/New%20folder(3)/New%20folder/src/main/java/com/company/eclms/common/storage/MinioStorageService.java) (Implementation)

| Method | Purpose |
|--------|---------|
| `@PostConstruct init()` | Connects to MinIO, checks if the default bucket exists, creates it if not. If connection fails → activates **fallback mode** (local filesystem under `logs/minio-fallback/`). |
| `uploadFile(bucket, name, stream, size, contentType)` | **Normal mode**: Creates bucket if needed → uploads via `minioClient.putObject()`. **Fallback mode**: Writes file to local directory. |
| `downloadFile(bucket, name)` | **Normal mode**: Returns `InputStream` from `minioClient.getObject()`. **Fallback mode**: Returns `InputStream` from local file. |
| `getPreviewUrl(bucket, name)` | **Normal mode**: Generates a pre-signed URL valid for 2 hours via `minioClient.getPresignedObjectUrl()`. **Fallback mode**: Returns a `file:///` URL. |
| `deleteFile(bucket, name)` | Removes file from MinIO or local filesystem. |

**Why fallback mode?** During local development, MinIO may not be running. The fallback ensures the app is still functional for development/testing purposes.

---

## 5. Backend: Feature Modules

### 5.1 Auth Module

**Package**: `com.company.eclms.modules.auth`

#### AuthController

**File**: [AuthController.java](file:///c:/Users/gagan/Downloads/New%20folder(3)/New%20folder/src/main/java/com/company/eclms/modules/auth/controller/AuthController.java)

| Endpoint | Method | Handler | Purpose |
|----------|--------|---------|---------|
| `POST /api/v1/auth/register` | `register()` | Delegates to `UserService.createUser()` | Public registration endpoint. Validates `UserRegistrationDto` with `@Valid`. |
| `POST /api/v1/auth/login` | `login()` | Delegates to `AuthService.login()` | Authenticates user, returns JWT tokens. Captures IP and User-Agent. |
| `POST /api/v1/auth/refresh` | `refresh()` | Delegates to `AuthService.refresh()` | Takes an expired access token's refresh token and issues new token pair. |
| `POST /api/v1/auth/logout` | `logout()` | Delegates to `AuthService.logout()` | Blacklists access token in Redis, revokes refresh token. |
| `POST /api/v1/auth/otp/send` | `sendOtp()` | Delegates to `AuthService.sendOtp()` | Generates 6-digit OTP for password reset, stores in Redis (5 min TTL). |
| `POST /api/v1/auth/password/reset` | `resetPassword()` | Delegates to `AuthService.resetPassword()` | Verifies OTP → resets password with BCrypt hash. |

**Annotations:**
| Annotation | Purpose |
|-----------|---------|
| `@RestController` | Combines `@Controller` + `@ResponseBody`. All return values are serialized to JSON. |
| `@RequestMapping("/api/v1/auth")` | Sets the base URL prefix for all endpoints in this controller. |
| `@Valid @RequestBody` | `@RequestBody` deserializes the JSON body into the DTO; `@Valid` triggers Jakarta Validation (`@NotBlank`, etc.) and throws `MethodArgumentNotValidException` on failure. |
| `@RequestHeader("Authorization")` | Binds the value of the HTTP `Authorization` header to the method parameter. |
| `@RequestParam` | Binds query parameters (e.g., `?email=user@example.com`) to method parameters. |

#### DTOs

| DTO | Fields | Validations |
|-----|--------|------------|
| `LoginRequest` | `username`, `password` | Both `@NotBlank` |
| `LoginResponse` | `accessToken`, `refreshToken`, `UserDto user` | `@Builder` for fluent construction |
| `RefreshTokenRequest` | `refreshToken` | `@NotBlank` |

#### AuthServiceImpl

**File**: [AuthServiceImpl.java](file:///c:/Users/gagan/Downloads/New%20folder(3)/New%20folder/src/main/java/com/company/eclms/modules/auth/service/impl/AuthServiceImpl.java)

**Key Features:**

1. **Account Lockout**: After 5 failed login attempts, account is locked for 15 minutes. Auto-unlocks when `lockedUntil` passes.

2. **Token Management**: Refresh tokens stored in Redis with 7-day TTL. On token refresh, old refresh token is replaced (token rotation) to prevent replay attacks.

3. **Token Blacklisting**: On logout, the access token is added to a Redis blacklist with 15-minute TTL (matching access token expiry).

4. **OTP System**: Generates 6-digit OTPs stored in Redis (5-min TTL). Falls back to in-memory `ConcurrentHashMap` if Redis is unavailable.

5. **Login History**: Each login attempt (success/failure) is recorded in `login_histories` table via `JdbcTemplate` for audit compliance.

6. **Redis Fallback**: All Redis operations are wrapped in try-catch with in-memory fallback maps — the app never crashes due to Redis unavailability.

---

### 5.2 User Module

**Package**: `com.company.eclms.modules.user`

#### User Entity

| Field | Type | Column | Purpose |
|-------|------|--------|---------|
| `username` | String | `UNIQUE, NOT NULL` | Login credential |
| `email` | String | `UNIQUE, NOT NULL` | For OTP-based password reset |
| `password` | String | — | BCrypt-encoded hash |
| `fullName` | String | — | Display name |
| `status` | String | Default `ACTIVE` | `ACTIVE`, `INACTIVE`, `SUSPENDED` |
| `locked` / `lockedUntil` | boolean / LocalDateTime | — | Account lockout state |
| `failedLoginAttempts` | int | Default 0 | Counter for brute-force protection |
| `department` | Department | `@ManyToOne` | User belongs to one department |
| `roles` | Set\<Role\> | `@ManyToMany` | User can have multiple roles (e.g., ADMIN + USER) |

**JPA Annotations:**
| Annotation | Purpose |
|-----------|---------|
| `@Entity` | Marks this class as a JPA entity (maps to a database table). |
| `@Table(name = "users")` | Explicitly names the table (otherwise JPA would use the class name). |
| `@SQLDelete(sql = "UPDATE users SET deleted = true...")` | **Hibernate** — intercepts `DELETE` operations and executes a soft-delete UPDATE instead. Includes `WHERE version = ?` for optimistic locking. |
| `@SQLRestriction("deleted = false")` | **Hibernate 6.3+** — automatically adds `WHERE deleted = false` to all queries on this entity, hiding soft-deleted records. |
| `@ManyToMany` + `@JoinTable` | Defines the many-to-many relationship with roles through the `user_roles` join table. `FetchType.EAGER` loads roles immediately with the user (needed for authentication). |
| `@ManyToOne(fetch = LAZY)` | Department is loaded only when accessed (performance optimization). |

#### UserRepository

```java
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    void softDeleteById(UUID id);
}
```

| Element | Purpose |
|---------|---------|
| `JpaRepository<User, UUID>` | Provides CRUD methods (`save`, `findById`, `findAll`, `deleteById`) + pagination. |
| `JpaSpecificationExecutor<User>` | Enables dynamic query building via `Specification<User>` — used for search/filter operations. |
| `findByUsername()` / `findByEmail()` | **Spring Data derived queries** — Spring generates the SQL from the method name. |
| `softDeleteById()` | Custom `@Query` with `@Modifying` — executes an UPDATE to set `deleted=true`. |

#### UserMapper

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
    User toEntity(UserDto userDto);
}
```

| Element | Purpose |
|---------|---------|
| `@Mapper(componentModel = "spring")` | **MapStruct** — generates an implementation class at compile time and registers it as a Spring bean. The generated code maps fields by name matching (e.g., `user.getUsername()` → `dto.setUsername()`). |
| `toDto()` / `toEntity()` | Conversion methods — MapStruct generates null-safe, type-safe mapping code. |

#### UserServiceImpl

**Key Methods:**

| Method | Purpose |
|--------|---------|
| `createUser()` | Checks for duplicate username/email → encodes password → assigns default USER role → saves. |
| `getUserById()` | Finds by ID, throws `NotFoundException` if missing. |
| `getAllUsers()` | Returns paginated list with search support via JPA Specifications. |
| `updateUser()` | Updates profile fields (name, email, status). Does NOT update password. |
| `deleteUser()` | Performs soft-delete via custom repository method. |
| `assignRole()` | Adds a role to a user's role set. |
| `lockUser()` / `unlockUser()` | Administrative account locking. |

---

### 5.3 Role Module

**Entity Fields**: `name` (ADMIN, USER), `description`, `permissions` (ManyToMany with permissions through `role_permissions` join table).

**Key Feature**: Each role has a set of permissions. When a user is assigned a role, they inherit all that role's permissions. This implements **Role-Based Access Control (RBAC)**.

---

### 5.4 Permission Module

**Entity Fields**: `name` (e.g., "CONTRACT_READ"), `description`, `permissionGroup` (e.g., "CONTRACT").

Permissions are the finest-grained authorization unit. Controller methods use `@PreAuthorize("hasAuthority('CONTRACT_READ')")` to check these.

---

### 5.5 Department Module

**Entity Fields**: `name`, `description`. Contracts belong to departments for organizational grouping.

---

### 5.6 Vendor Module

**Entity Fields**: `name`, `email`, `phone`, `address`, `gstNumber`, `panNumber`, `riskLevel` (LOW/MEDIUM/HIGH).

Vendors are the external parties with whom contracts are established. `riskLevel` is used for compliance tracking.

---

### 5.7 Contract Module

**Package**: `com.company.eclms.modules.contract`

This is the **core business module**.

#### Contract Entity

| Field | Purpose |
|-------|---------|
| `name` | Contract title |
| `vendor` | `@ManyToOne` — the vendor party |
| `department` | `@ManyToOne` — the owning department |
| `template` | `@ManyToOne` — optional reference to the template used to generate content |
| `content` | `TEXT` — the actual contract text (may be interpolated from a template) |
| `status` | Lifecycle state: `DRAFT` → `IN_REVIEW` → `ACTIVE`/`REJECTED` → `EXPIRED`/`TERMINATED` |
| `startDate` / `endDate` / `renewalDate` | Contract temporal bounds |
| `riskScore` | Decimal risk assessment value |
| `riskAssessment` | Text description of risk factors |
| `metadata` | Free-form JSON/text metadata |
| `versionString` | Human-readable version (e.g., "1.0", "2.0" after renewal) |

#### ContractServiceImpl — Key Methods

| Method | Logic |
|--------|-------|
| `createContract()` | Resolves vendor + department by ID → if template specified, loads template and calls `interpolate()` to replace `{{variable}}` placeholders with provided values → saves with status DRAFT, version 1.0. |
| `getContracts()` | Builds a **JPA Specification** dynamically: filters by status, departmentId, vendorId, and text search (LIKE on name/content). Returns paginated results. |
| `updateContract()` | Only allows updates when status is DRAFT or REJECTED. Re-interpolates template if template variables changed. |
| `deleteContract()` | Soft-delete via `contractRepository.softDeleteById()`. |
| `renewContract()` | Only for ACTIVE or EXPIRED contracts. Increments version by 1.0, extends endDate, sets renewalDate to 1 month before new end, resets status to DRAFT for re-approval. |
| `terminateContract()` | Sets status to TERMINATED. |
| `interpolate(content, variables)` | Replaces `{{key}}` placeholders in template content with provided values. Used when creating/updating contracts from templates. |

---

### 5.8 Contract Template Module

Templates contain reusable contract text with `{{variable}}` placeholders.

#### ContractTemplate Entity

| Field | Purpose |
|-------|---------|
| `name` | Template name (e.g., "Standard Vendor Agreement") |
| `content` | `TEXT` — template text with `{{vendor_name}}`, `{{amount}}`, etc. |
| `variables` | Auto-extracted comma-separated list of placeholders found in content |
| `versionString` | Tracks template versions (incremented by 0.1 on update) |
| `status` | `DRAFT` → `PUBLISHED` → `ARCHIVED` |

#### ContractTemplateServiceImpl

| Method | Purpose |
|--------|---------|
| `createTemplate()` | Saves with DRAFT status, version 1.0. Auto-extracts variables from content via regex `\{\{([^}]+)\}\}`. |
| `updateTemplate()` | If PUBLISHED, bumps version by 0.1 and returns to DRAFT. Re-extracts variables from new content. |
| `publishTemplate()` | Sets status to PUBLISHED. Throws `ConflictException` if already published. |
| `archiveTemplate()` | Sets status to ARCHIVED (no longer selectable for new contracts). |
| `extractVariables()` | Uses regex to find all `{{...}}` patterns and returns a comma-separated string of variable names. |

---

### 5.9 Document Module

**Purpose**: Manages file uploads and downloads linked to contracts.

#### Document Entity

| Field | Purpose |
|-------|---------|
| `fileName` | Original uploaded filename |
| `filePath` | Storage path in MinIO or local fallback |
| `fileType` | MIME type (e.g., "application/pdf") |
| `checksum` | SHA-256 hash for integrity verification |
| `docSize` | File size in bytes |
| `versionNumber` | Document version tracking |
| `ocrText` | Extracted text from OCR (for search indexing) |
| `metadata` | Additional document metadata (JSON text) |
| `contract` | `@ManyToOne` — optional link to a contract |

#### DocumentServiceImpl

| Method | Purpose |
|--------|---------|
| `uploadDocument(file, contractId)` | Computes SHA-256 checksum → uploads to MinIO via `StorageService` → saves metadata to database. If `contractId` provided, links the document to that contract. |
| `downloadDocument(id)` | Retrieves file from MinIO/local storage as `InputStream`. |
| `getPreviewUrl(id)` | Generates a pre-signed URL for browser-based file preview. |
| `deleteDocument(id)` | Deletes from both storage and database. |
| `getDocumentsByContract(contractId)` | Returns all documents linked to a specific contract. |

---

### 5.10 Workflow Module

**The most complex module** — implements a multi-step approval engine.

#### Entities

| Entity | Purpose |
|--------|---------|
| `Workflow` | A reusable workflow template (e.g., "Standard Vendor Approval"). Contains a list of `WorkflowStep` objects. |
| `WorkflowStep` | A single step in a workflow. Has `stepNumber`, `stepType` (SEQUENTIAL), `assigneeRole` (which role can approve), `assigneeUser` (optional specific user), `requiredApprovals` (how many approvals needed). |
| `WorkflowInstance` | A running instance of a workflow tied to a specific contract. Tracks `currentStepNumber` and `status` (IN_PROGRESS, APPROVED, REJECTED). |
| `WorkflowApproval` | An individual approval/rejection action by a user. Records `stepNumber`, `user`, `status` (APPROVED/REJECTED), `comments`, `actionDate`. |

#### WorkflowServiceImpl — Key Methods

| Method | Purpose |
|--------|---------|
| `createWorkflow()` | Creates a workflow template with steps. Sets back-references on steps (step → workflow). |
| `startWorkflow(workflowId, contractId)` | Creates a `WorkflowInstance` linked to the contract. Checks for existing IN_PROGRESS instance (prevents duplicate workflows). Sets contract status to IN_REVIEW. |
| `approveOrRejectStep(instanceId, userId, request)` | **Core approval engine**: (1) Validates instance is IN_PROGRESS, (2) Gets current step from workflow template, (3) **Authorization check** — verifies user has the required role or is the specific assignee or is ADMIN, (4) Records the approval/rejection, (5) **If REJECTED** → instance REJECTED, contract REJECTED, (6) **If APPROVED** → counts approvals for current step, if enough → advance to next step. If last step → instance APPROVED, contract ACTIVE. |
| `getApprovalHistory(instanceId)` | Returns all approval actions for an instance, ordered by date descending. |

**Step Advancement Logic:**
```
APPROVED on Step N:
  count = approvals for step N where status = "APPROVED"
  if count >= step.requiredApprovals:
    if N < totalSteps:
      advance to step N+1
    else:
      workflow APPROVED, contract → ACTIVE
```

---

### 5.11 Dashboard Module

**Provides aggregated statistics** for the dashboard view.

#### DashboardController

| Endpoint | Data Returned |
|----------|--------------|
| `GET /api/v1/dashboard/stats` | Total contracts, active contracts, pending (in-review) contracts, total vendors, total users, total templates, total documents, upcoming renewals (within 30 days) |

The service queries multiple repositories and returns a `DashboardDto` with all counts.

---

### 5.12 Report Module

**Generates downloadable reports** in multiple formats.

#### ReportController

| Endpoint | Format | Implementation |
|----------|--------|----------------|
| `GET /api/v1/reports/export/excel` | `.xlsx` | Apache POI `XSSFWorkbook` |
| `GET /api/v1/reports/export/pdf` | `.pdf` | OpenPDF `PdfWriter` |
| `GET /api/v1/reports/export/csv` | `.csv` | `StringBuilder` |

#### ReportServiceImpl

| Method | How It Works |
|--------|-------------|
| `generateContractsExcelReport()` | Loads all contracts → creates Excel workbook with header row → iterates contracts adding data rows → returns as `ByteArrayInputStream`. |
| `generateContractsPdfReport()` | Loads all contracts → creates PDF document with landscape A4 → adds title → creates 8-column table with headers and data → returns as `ByteArrayInputStream`. |
| `generateContractsCsvReport()` | Loads all contracts → builds CSV string with proper escaping (double-quotes inside values) → returns as `String` which controller wraps in `InputStreamResource`. |

---

## 6. Frontend (Angular)

### Architecture

The frontend is an Angular 19 SPA using **standalone components** (no NgModules).

```
ECLMS_FE/src/app/
├── core/                    # Singleton services, guards, interceptors
│   ├── guards/
│   │   └── auth.guard.ts    # Route protection
│   ├── interceptors/
│   │   └── jwt.interceptor.ts  # Auto-attaches JWT to requests
│   └── services/
│       ├── auth.service.ts      # Login/logout/token management
│       ├── contract.service.ts  # Contract CRUD API calls
│       ├── dashboard.service.ts # Dashboard stats
│       ├── department.service.ts
│       ├── document.service.ts  # File upload/download
│       ├── report.service.ts    # Report downloads
│       ├── template.service.ts  # Template CRUD
│       ├── vendor.service.ts
│       └── workflow.service.ts  # Workflow actions
├── features/                # Feature-specific components
│   ├── contracts/           # Contract list + form
│   ├── dashboard/           # Dashboard view
│   ├── documents/           # Document management
│   ├── login/               # Login page
│   ├── metadata/            # Roles, Departments, Vendors, Permissions CRUD
│   ├── register/            # Registration page
│   ├── templates/           # Template management
│   └── workflows/           # Workflow management
└── shared/
    └── layout/              # Sidebar + navbar layout wrapper
```

### App Configuration

**File**: [app.config.ts](file:///c:/Users/gagan/Downloads/New%20folder(3)/New%20folder/ECLMS_FE/src/app/app.config.ts)

```typescript
export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),  // Optimization: batches change detection
    provideRouter(routes),                                    // Registers route definitions
    provideHttpClient(withInterceptors([jwtInterceptor]))    // HTTP client with JWT interceptor
  ]
};
```

### Routing

**File**: [app.routes.ts](file:///c:/Users/gagan/Downloads/New%20folder(3)/New%20folder/ECLMS_FE/src/app/app.routes.ts)

| Path | Component | Guard | Purpose |
|------|-----------|-------|---------|
| `/login` | `LoginComponent` | None | Public login page |
| `/register` | `RegisterComponent` | None | Public registration page |
| `/` | `LayoutComponent` | `authGuard` | **Protected layout wrapper** — all child routes require authentication |
| `/dashboard` | `DashboardComponent` | (inherited) | Dashboard statistics |
| `/contracts` | `ContractListComponent` | (inherited) | Contract list with search/filter |
| `/contracts/new` | `ContractFormComponent` | (inherited) | Create new contract |
| `/contracts/edit/:id` | `ContractFormComponent` | (inherited) | Edit existing contract |
| `/workflows` | `WorkflowListComponent` | (inherited) | Workflow instances & approvals |
| `/documents` | `DocumentListComponent` | (inherited) | Document upload/download |
| `/templates` | `TemplateListComponent` | (inherited) | Template management |
| `/metadata` | `MetadataListComponent` | (inherited) | Departments, Vendors, Roles, Permissions CRUD |
| `**` | Redirect → `/dashboard` | — | Catch-all wildcard |

### Auth Guard

```typescript
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  if (authService.isLoggedIn()) {
    return true;
  }
  router.navigate(['/login']);
  return false;
};
```

| Element | Purpose |
|---------|---------|
| `CanActivateFn` | Angular functional guard (v15+ pattern, replacing class-based guards). |
| `inject(AuthService)` | Angular's `inject()` function — gets service from DI container (replaces constructor injection in functional guards). |
| `isLoggedIn()` | Checks if a valid JWT exists in localStorage. |

### JWT Interceptor

```typescript
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }
  return next(req);
};
```

**Purpose**: Automatically attaches the JWT access token to every outgoing HTTP request's `Authorization` header. Uses `req.clone()` because Angular `HttpRequest` objects are immutable.

### AuthService

| Method | Purpose |
|--------|---------|
| `login(username, password)` | Calls `POST /api/v1/auth/login` → on success, stores `accessToken`, `refreshToken`, and `currentUser` in `localStorage` and `BehaviorSubject`. |
| `logout()` | Calls `POST /api/v1/auth/logout` → clears localStorage → navigates to `/login`. |
| `register(data)` | Calls `POST /api/v1/auth/register`. |
| `isLoggedIn()` | Checks if `accessToken` exists in localStorage. |
| `currentUserValue` | Returns the current user from the `BehaviorSubject`. |

### Layout Component

**File**: [layout.component.ts](file:///c:/Users/gagan/Downloads/New%20folder(3)/New%20folder/ECLMS_FE/src/app/shared/layout/layout.component.ts)

Provides the **sidebar navigation** (Dashboard, Contracts, Templates, Workflows, Documents, Entities) and **top navbar** with user profile and logout button. Uses `<router-outlet>` to render child route components.

### Service Layer Pattern

All services follow the same pattern:
```typescript
@Injectable({ providedIn: 'root' })   // Singleton service, tree-shakeable
export class ContractService {
  private http = inject(HttpClient);   // Angular DI
  private apiUrl = `${environment.apiUrl}/contracts`;  // Base URL from environment

  getContracts(page, size, search, status): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size)...;
    return this.http.get<any>(this.apiUrl, { params });
  }
}
```

| Angular Concept | Purpose |
|----------------|---------|
| `@Injectable({ providedIn: 'root' })` | Registers the service in the root injector. Only one instance exists app-wide. `providedIn: 'root'` also makes it tree-shakeable — if no component uses it, it's excluded from the production bundle. |
| `inject(HttpClient)` | Modern Angular DI (v14+) — alternative to constructor injection. |
| `Observable<any>` | RxJS observable — HTTP calls are lazy (only execute when subscribed to by a component). |
| `HttpParams` | Immutable query parameter builder. Each `.set()` returns a new `HttpParams` instance. |

---

## 7. Database Schema & Migrations

The database schema is managed by **Flyway** migrations located at `src/main/resources/db/migration/mysql/`.

### Tables & Relationships

```
┌─────────────┐     ┌──────────────┐     ┌─────────────────────┐
│ permissions  │◄────┤role_permissions├───►│       roles          │
└─────────────┘     └──────────────┘     └──────────┬──────────┘
                                                     │
                    ┌──────────────┐                  │
                    │ user_roles   ├──────────────────┘
                    └──────┬───────┘
                           │
┌─────────────┐     ┌──────┴──────┐     ┌──────────────┐
│ departments  │◄────┤    users    ├────►│   teams      │
└──────┬──────┘     └──────┬──────┘     │ team_members │
       │                   │             └──────────────┘
       │            ┌──────┴──────┐
       │            │  contracts  │◄────── contract_templates
       └───────────►│             │
                    └──────┬──────┘
                           │
            ┌──────────────┼──────────────┐
            │              │              │
    ┌───────┴──────┐ ┌─────┴──────┐ ┌─────┴──────────────┐
    │  documents   │ │  vendors   │ │ workflow_instances  │
    └──────────────┘ └────────────┘ └──────────┬─────────┘
                                               │
                                    ┌──────────┴──────────┐
                                    │ workflow_approvals   │
                                    └─────────────────────┘
                                    
    ┌──────────────┐     ┌─────────────┐     ┌──────────────────┐
    │  workflows   │◄────┤workflow_steps│     │ audit_logs       │
    └──────────────┘     └─────────────┘     │ login_histories  │
                                             └──────────────────┘
```

### Key Indexes

| Index | Table | Column(s) | Purpose |
|-------|-------|-----------|---------|
| `idx_contracts_vendor` | contracts | vendor_id | Fast joins when filtering by vendor |
| `idx_contracts_department` | contracts | department_id | Fast joins when filtering by department |
| `idx_contracts_status` | contracts | status | Fast filtering by contract status |
| `idx_users_username` | users | username | Fast login lookups |
| `idx_users_email` | users | email | Fast OTP/password-reset lookups |
| `idx_audit_entity` | audit_logs | entity_name, entity_id | Fast audit trail lookups |

---

## 8. Annotations & Decorators Reference Guide

### Java / Spring Boot Annotations

| Annotation | Type | Purpose | Where Used |
|-----------|------|---------|------------|
| **`@SpringBootApplication`** | Spring Boot | Meta-annotation: `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan` | Main class |
| **`@Configuration`** | Spring | Marks class as a source of `@Bean` definitions | Config classes |
| **`@Bean`** | Spring | Marks a method whose return value is registered as a Spring bean | Config classes |
| **`@Component`** | Spring | Registers class as a Spring-managed component (generic) | Filters, audit |
| **`@Service`** | Spring | Specialization of `@Component` for service-layer classes | All services |
| **`@Repository`** | Spring | Specialization of `@Component` for data-access classes. Also translates persistence exceptions to Spring's `DataAccessException`. | All repositories |
| **`@RestController`** | Spring Web | `@Controller` + `@ResponseBody` — returns JSON directly | All controllers |
| **`@RequestMapping`** | Spring Web | Maps HTTP requests to handler methods or classes | Controller base URL |
| **`@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping` / `@PatchMapping`** | Spring Web | Shorthand for `@RequestMapping(method = GET/POST/PUT/DELETE/PATCH)` | Controller methods |
| **`@RequestBody`** | Spring Web | Deserializes HTTP request body (JSON) into a Java object | Controller params |
| **`@RequestParam`** | Spring Web | Binds query parameters (e.g., `?key=value`) to method parameters | Controller params |
| **`@PathVariable`** | Spring Web | Extracts path segments (e.g., `/users/{id}`) into method parameters | Controller params |
| **`@RequestHeader`** | Spring Web | Binds an HTTP header value to a method parameter | Auth controller |
| **`@Valid`** | Jakarta Validation | Triggers bean validation on the annotated parameter | DTOs |
| **`@NotBlank`** | Jakarta Validation | Field must not be null, empty, or whitespace-only | DTO fields |
| **`@Size`** | Jakarta Validation | Field length must be within min/max bounds | DTO fields |
| **`@Email`** | Jakarta Validation | Field must be a valid email format | DTO fields |
| **`@Entity`** | JPA | Marks class as a persistent entity (maps to a DB table) | All entities |
| **`@Table`** | JPA | Specifies the table name | All entities |
| **`@Id`** | JPA | Marks the primary key field | BaseEntity |
| **`@GeneratedValue`** | JPA | Specifies ID generation strategy | BaseEntity |
| **`@Column`** | JPA | Customizes column mapping (name, nullable, length, etc.) | All entities |
| **`@ManyToOne`** | JPA | Defines a many-to-one relationship (e.g., many contracts → one vendor) | Entity relationships |
| **`@OneToMany`** | JPA | Defines a one-to-many relationship (e.g., one workflow → many steps) | Entity relationships |
| **`@ManyToMany`** | JPA | Defines a many-to-many relationship (e.g., users ↔ roles) | User-Role, Role-Permission |
| **`@JoinColumn`** | JPA | Specifies the foreign key column | ManyToOne/OneToMany |
| **`@JoinTable`** | JPA | Specifies the join table for ManyToMany relationships | User-Role |
| **`@MappedSuperclass`** | JPA | This class's fields are inherited by entity subclasses (no separate table) | BaseEntity |
| **`@Version`** | JPA | Enables optimistic locking with automatic version incrementing | BaseEntity |
| **`@EntityListeners`** | JPA | Registers lifecycle event listeners | BaseEntity |
| **`@CreatedDate` / `@LastModifiedDate`** | Spring Data | Auto-populate with timestamps on create/update | BaseEntity |
| **`@CreatedBy` / `@LastModifiedBy`** | Spring Data | Auto-populate with current user on create/update | BaseEntity |
| **`@SQLDelete`** | Hibernate | Overrides DELETE SQL to implement soft-delete | Entity classes |
| **`@SQLRestriction`** | Hibernate 6.3+ | Adds WHERE clause to all queries (hides soft-deleted records) | Entity classes |
| **`@EnableJpaAuditing`** | Spring Data JPA | Activates the auditing infrastructure | JpaAuditingConfig |
| **`@EnableWebSecurity`** | Spring Security | Enables Spring Security's web security configuration | SecurityConfig |
| **`@EnableMethodSecurity`** | Spring Security | Enables `@PreAuthorize` / `@PostAuthorize` annotations | SecurityConfig |
| **`@PreAuthorize`** | Spring Security | SpEL expression evaluated before method execution for authorization | Controller methods |
| **`@Transactional`** | Spring TX | Wraps method in a database transaction. `readOnly = true` is a performance hint. | Service methods |
| **`@Modifying`** | Spring Data | Required alongside `@Query` for UPDATE/DELETE queries | Repository methods |
| **`@Query`** | Spring Data | Defines custom JPQL/SQL query on a repository method | Repository methods |
| **`@Value`** | Spring | Injects values from `application.properties` using `${property.key}` syntax | Config fields |
| **`@PostConstruct`** | Jakarta | Method called once after bean construction and dependency injection | Init methods |
| **`@Slf4j`** | Lombok | Generates `private static final Logger log = LoggerFactory.getLogger(...)` | Most classes |
| **`@Getter` / `@Setter`** | Lombok | Generates getter/setter methods for all fields | Entities, DTOs |
| **`@Data`** | Lombok | `@Getter` + `@Setter` + `@ToString` + `@EqualsAndHashCode` + `@RequiredArgsConstructor` | DTOs |
| **`@Builder`** | Lombok | Generates the Builder pattern | DTOs, CustomUserDetails |
| **`@NoArgsConstructor`** | Lombok | Generates no-argument constructor (required by JPA and Jackson) | DTOs, entities |
| **`@AllArgsConstructor`** | Lombok | Generates constructor with all fields as parameters | DTOs |
| **`@RequiredArgsConstructor`** | Lombok | Generates constructor with `final` fields only (used for DI) | Services, controllers |
| **`@Mapper`** | MapStruct | Marks an interface as a bean mapper; generates implementation at compile time | All mappers |
| **`@RestControllerAdvice`** | Spring | Global exception handler for all `@RestController` classes | GlobalExceptionHandler |
| **`@ExceptionHandler`** | Spring | Maps exception types to handler methods | GlobalExceptionHandler |

### TypeScript / Angular Decorators

| Decorator / Function | Purpose |
|----------------------|---------|
| **`@Component`** | Marks a class as an Angular component with metadata (selector, template, styles) |
| **`@Injectable`** | Marks a class as injectable (can be used with Angular's DI system) |
| **`providedIn: 'root'`** | Registers the service as a singleton in the root injector |
| **`standalone: true`** | Component does not belong to any NgModule — can be directly imported |
| **`inject(Service)`** | Angular's inject function — retrieves a dependency from the DI container (functional DI) |
| **`CanActivateFn`** | Type for functional route guards (Angular 15+) |
| **`HttpInterceptorFn`** | Type for functional HTTP interceptors (Angular 15+) |
| **`Observable<T>`** | RxJS type — represents a lazy, asynchronous data stream |
| **`BehaviorSubject<T>`** | RxJS type — observable that stores the latest value and emits it to new subscribers |

---

## Summary

This ECLMS project is a **production-grade enterprise application** implementing:

- **JWT-based stateless authentication** with access/refresh token rotation
- **Role-Based Access Control (RBAC)** with 43 fine-grained permissions
- **Multi-step approval workflows** with role-based authorization
- **Soft-delete** across all entities with audit trails
- **Optimistic locking** for concurrent modification safety
- **Rate limiting** with Redis (primary) and in-memory (fallback)
- **Full-text search** via Elasticsearch
- **Object storage** via MinIO with local filesystem fallback
- **Report generation** in Excel, PDF, and CSV formats
- **Database migrations** via Flyway
- **API documentation** via Swagger/OpenAPI
- **Template-based contract generation** with variable interpolation
- **Account lockout** after failed login attempts
- **Graceful degradation** — Redis, Elasticsearch, and MinIO failures do not crash the application
