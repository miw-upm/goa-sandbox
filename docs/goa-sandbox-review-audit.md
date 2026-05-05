# goa-sandbox Review Audit

> Audit date: 2026-05-05
> Auditor: M1.1 only — no business code written
> goa-sandbox branch: feature/reviews (up to date with origin/feature/reviews)

---

## Current branch and git status

- branch: `feature/reviews`
- status: `nothing to commit, working tree clean`
- latest commit: `3ace54e docs: define customer reviews scope`
- goa-front: NOT inspected (out of scope for M1.1)

---

## Project structure summary

```
src/main/java/es/upm/api/
├── Application.java                          ← Spring Boot main class
├── configurations/
│   ├── TokenManager.java                     ← OAuth2 token management
│   ├── OpenApiConfig.java                    ← Swagger/OpenAPI config
│   ├── ResourceServerConfig.java             ← Security filter chains (JWT)
│   ├── FeignConfig.java                      ← OpenFeign client config
│   ├── LoggingFilter.java                    ← Request logging
│   ├── DatabaseSeederDev.java                ← Dev seed data
│   └── EurekaConfig.java                     ← Service discovery
├── domain/
│   ├── model/
│   │   ├── Complaint.java                    ← Domain entity (plain POJO + Lombok)
│   │   ├── ComplaintState.java               ← Enum: OPEN, CLOSED
│   │   └── external/
│   │       └── UserSnapshot.java            ← User read model (Lombok, UUID id)
│   ├── services/
│   │   └── ComplaintService.java             ← Business logic (constructor injection)
│   └── ports/out/
│       ├── ComplaintRepository.java          ← Repository interface (domain layer)
│       └── user/
│           └── UserFinder.java               ← User read port interface
├── adapter/
│   ├── in/
│   │   └── resources/
│   │       ├── ComplaintResource.java        ← REST controller
│   │       ├── SystemResource.java           ← Health/badge endpoints
│   │       └── httperrors/
│   │           └── ApiExceptionHandler.java  ← @ControllerAdvice exception mapping
│   └── out/
│       ├── complaint/
│       │   ├── ComplaintEntity.java           ← MongoDB document (Lombok + @Document)
│       │   ├── ComplaintMongoRepository.java ← Spring Data MongoRepository
│       │   └── ComplaintPersistenceMongodb.java ← Repository implementation (adapter)
│       ├── user/feign/
│       │   ├── GoaUserClient.java            ← OpenFeign interface
│       │   └── UserFinderAdapter.java        ← Feign adapter implementing UserFinder
│       └── legal/mongo/xxx/
│           ├── XxxEntity.java                ← Empty stub (future module)
│           └── XxxAdapter.java               ← Empty stub (future module)

src/test/java/es/upm/api/
├── functionaltests/
│   └── SystemResourceFT.java                ← SpringBootTest + TestRestTemplate
├── domain/services/
│   └── ComplaintServiceTest.java             ← @ExtendWith(MockitoExtension) unit test
└── adapter/in/resources/
    └── ComplaintResourceIT.java             ← @SpringBootTest + @AutoConfigureMockMvc + JWT mock
```

**Total Java source files:** 24
**Total test files:** 3

---

## Existing architecture style

**Mixed hexagonal / layered Spring Boot architecture**, strongly influenced by domain-driven design.

- **Domain layer** (`domain/`): holds pure business model (`Complaint`) and domain service (`ComplaintService`) with business rules.
- **Port interfaces** (`domain/ports/out/`): repository interfaces defined at domain level, not in adapter.
- **Adapters** (`adapter/`): split into `in/` (driving/adapter-in) and `out/` (driven/adapter-out).
  - `in/resources/`: REST controllers.
  - `out/complaint/`: MongoDB persistence adapter implementing domain port.
  - `out/user/feign/`: outbound Feign client for user microservice.
- **Configurations** (`configurations/`): cross-cutting concerns (security, OpenAPI, Feign, Eureka).

This is **NOT** a classic 3-layer (Controller → Service → DAO) monolith. It follows a **hexagonal (ports & adapters)** architecture with domain at the center.

---

## Resource / controller pattern

| Item | Detail |
|---|---|
| File | `adapter/in/resources/ComplaintResource.java` |
| Annotation | `@RestController` + `@RequestMapping("/complaints")` |
| CORS | `@CrossOrigin(origins = "http://localhost:4200")` on controller class |
| Dependency injection | Constructor injection with `@Autowired` |
| Input validation | `@Valid` on request body parameter |
| Security | `@PreAuthorize("hasRole('CUSTOMER') or hasRole('MANAGER') or hasRole('ADMIN')")` on method |
| Auth user extraction | `@AuthenticationPrincipal Jwt jwt` + `jwt.getClaimAsString("username")` |
| Returns | Domain model directly (`Complaint`) — NOT a DTO wrapper |
| HTTP method | Single endpoint: `POST /complaints` (create) |

**Important observation:** The resource returns the **domain object** (`Complaint`), not a DTO. There is no separate response DTO in the current Complaint implementation.

---

## DTO pattern

**No DTOs found for Complaint.** The current implementation uses the domain object directly as both the API request body and response body.

For UserSnapshot (external model), Lombok is used with `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`.

**Likely DTO pattern to follow for Review (inferred):**
- `ReviewDto` (response, Lombok `@Data`, with `UserSnapshot` or user reference)
- `ReviewCreateDto` / `ReviewUpdateDto` (request bodies, with `@NotNull`, `@Min`, `@Max`, `@NotBlank`)
- `BeanUtils.copyProperties()` for entity ↔ domain mapping (seen in `ComplaintEntity`)

---

## Service pattern

| Item | Detail |
|---|---|
| Location | `domain/services/ComplaintService.java` |
| Type | Concrete class with `@Service` (no interface at domain level) |
| Dependency injection | Constructor injection with `@Autowired` |
| Business validation | Manual null checks + `ResponseStatusException(HttpStatus.BAD_REQUEST)` |
| Business rules | Enforced in service (e.g., duplicate check → `HttpStatus.CONFLICT`) |
| ID generation | SHA-256 hash in service (`DigestUtils.sha256Hex`) |
| Timestamps | `LocalDateTime.now()` set in service |
| Error handling | Throws `ResponseStatusException` with `HttpStatus` |

---

## Repository / persistence pattern

| Item | Detail |
|---|---|
| Entity location | `adapter/out/complaint/ComplaintEntity.java` |
| Repository interface | `ComplaintMongoRepository` extends `MongoRepository<ComplaintEntity, String>` |
| Persistence adapter | `ComplaintPersistenceMongodb implements ComplaintRepository` (domain port) |
| Entity annotation | `@Document(collection = "complaints")`, `@Id` on `String id` |
| Lombok on entity | `@Data`, `@NoArgsConstructor` |
| ID type | `String` (not UUID, not Long) |
| Mapping | Entity ↔ Domain: `BeanUtils.copyProperties()` bidirectional constructor |
| Date type | `LocalDateTime` for timestamps |
| No `@CreatedDate` / `@LastModifiedDate` annotations found |
| Unique constraint | Implemented in service (check before create), NOT via MongoDB unique index |

---

## Security and current user pattern

| Item | Detail |
|---|---|
| Auth method | OAuth2 JWT Bearer token via `spring-boot-starter-oauth2-resource-server` |
| JWT extraction | `@AuthenticationPrincipal Jwt jwt` in controller |
| User ID field | `jwt.getClaimAsString("username")` — returns the username string |
| Roles claim | JWT claim name `"roles"` mapped to Spring Security authorities with `ROLE_` prefix |
| JWT converter | `JwtGrantedAuthoritiesConverter` with `ROLE_` prefix |
| Security config | Two filter chains: system endpoints (permitAll) + API endpoints (authenticated) |
| Method-level security | `@EnableMethodSecurity` + `@PreAuthorize("hasRole('CUSTOMER')")` |
| Test JWT mock | `SecurityMockMvcRequestPostProcessors.jwt()` with `.claim("username", "6")` and `.authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))` |

**Current user pattern found in ComplaintResource:**
```java
@PreAuthorize("hasRole('CUSTOMER') or hasRole('MANAGER') or hasRole('ADMIN')")
public Complaint create(@Valid @RequestBody Complaint complaint, @AuthenticationPrincipal Jwt jwt) {
    String userId = jwt.getClaimAsString("username");
    complaint.setUserId(userId);
    return this.complaintService.create(complaint);
}
```

**IMPORTANT:** The username claim value "6" in test corresponds to the mobile number. This means `userId` in the domain model is actually the **mobile string**, not a UUID.

---

## EngagementLetter / Hoja de encargo evidence

**Status: NOT FOUND in goa-sandbox.**

The goa-sandbox codebase does not contain any class named `EngagementLetter`, `HojaDeEncargo`, `Letter`, `Encargo`, or similar. The Complaint entity has a `barcode` field which is described as "Hoja de Encargo" in comments, but no EngagementLetter entity exists.

**Implications:**
- goa-sandbox does NOT own EngagementLetter data.
- Review must reference EngagementLetter by ID only (barcode), per scope document rule: "do not access another microservice database directly."
- The goa-front or another microservice (e.g., `goa-engagement`) owns EngagementLetter data.
- Review's `letterId` field should be a `String barcode` following the Complaint pattern, not a foreign key object.

---

## Best implementation target for Review later

Based on the Complaint module audit, the most faithful implementation for Review should mirror the hexagonal pattern:

| Layer | Target location | Pattern to follow |
|---|---|---|
| Domain model | `domain/model/Review.java` | Lombok `@Data @Builder @NoArgsConstructor @AllArgsConstructor`, `String id`, `LocalDateTime`, `String userId` |
| Domain enum | `domain/model/ReviewState.java` (if needed) | Enum like `ComplaintState` |
| Domain service | `domain/services/ReviewService.java` | Constructor injection, business rules, ID generation |
| Port interface | `domain/ports/out/ReviewRepository.java` | Interface with `create()`, `read()`, `update()`, `findByUserId()` |
| Resource | `adapter/in/resources/ReviewResource.java` | `@RestController @RequestMapping("/reviews")`, `@PreAuthorize("hasRole('CUSTOMER')")`, JWT extraction |
| Entity | `adapter/out/review/ReviewEntity.java` | `@Document(collection = "reviews")`, Lombok, `BeanUtils.copyProperties()` |
| MongoRepository | `adapter/out/review/ReviewMongoRepository.java` | `extends MongoRepository<ReviewEntity, String>` |
| Persistence adapter | `adapter/out/review/ReviewPersistenceMongodb.java` | Implements `ReviewRepository`, `save()` + `findById()` |
| Feign client | `adapter/out/engagement/EngagementClient.java` | For fetching EngagementLetter by barcode (if allowed) |
| Exception | Throws `ResponseStatusException` with `HttpStatus.NOT_FOUND`, `HttpStatus.CONFLICT`, `HttpStatus.FORBIDDEN` |
| Unit test | `src/test/java/es/upm/api/domain/services/ReviewServiceTest.java` | `@ExtendWith(MockitoExtension.class)` |
| Integration test | `src/test/java/es/upm/api/adapter/in/resources/ReviewResourceIT.java` | `@SpringBootTest @AutoConfigureMockMvc` + JWT mock |

**Request DTOs to create (under `adapter/in/resources/dto/` or `dto/`):**
- `ReviewCreateDto`: `@NotNull stars (1-5)`, `@NotBlank opinion`, `@NotNull letter (barcode)`
- `ReviewUpdateDto`: stars, opinion fields with same validations

**Existing classes to reference for patterns:**
- `ComplaintResource.java` → resource/controller pattern
- `ComplaintService.java` → business logic pattern
- `ComplaintEntity.java` → MongoDB entity pattern
- `ComplaintPersistenceMongodb.java` → repository adapter pattern
- `UserSnapshot.java` → Lombok domain model pattern
- `ComplaintResourceIT.java` → integration test with JWT mock pattern
- `ComplaintServiceTest.java` → unit test with Mockito pattern

---

## Risks and open questions before M2

| # | Question | Severity | Notes |
|---|---|---|---|
| 1 | **What is the exact type of `userId`?** | HIGH | Complaint uses `String mobile`. JWT claim `"username"` returns `"6"` (mobile) in test. Review should use `String userId` following Complaint pattern. Need to confirm if `username` = mobile or UUID. |
| 2 | **How does the frontend get the user's `userId`?** | HIGH | JWT `username` claim (mobile string) — not UUID. Frontend must send JWT token, backend extracts `username`. goa-front must configure Angular HTTP interceptor to attach JWT. |
| 3 | **What is the EngagementLetter identifier?** | HIGH | Complaint uses `barcode` as the letter reference. Review should likely use the same `barcode` field. Need to confirm EngagementLetter service exposes barcode. |
| 4 | **Is there a Feign client for EngagementLetter?** | MEDIUM | Currently no `EngagementLetterClient`. If Review needs to validate the letter belongs to the customer, a Feign call to `goa-engagement` may be needed. This may be out of scope. |
| 5 | **How does the backend verify the letter belongs to the current user?** | HIGH | Scope says "customer can only see their own Hojas de encargo." Does this mean Review create/update must verify the barcode belongs to the JWT user? Not yet found in code. |
| 6 | **Is there a database migration mechanism?** | MEDIUM | MongoDB schema is managed by Spring Data (no Flyway/Liquibase found). New collections are created automatically. No explicit migration needed. |
| 7 | **What is the gateway path prefix?** | MEDIUM | docker-compose.yml comments say `/api/goa-sandbox/complaints` via Gateway. Need to confirm gateway routes for `/reviews`. |
| 8 | **Is there an existing "unique per user per letter" pattern?** | MEDIUM | Complaint enforces one OPEN complaint per barcode+mobile via service check. Review must enforce one review per customer per Engagement Letter (no delete per scope). `HttpStatus.CONFLICT` if duplicate. |
| 9 | **Does goa-sandbox have a shared `dto` package convention?** | LOW | No DTOs currently in the project (domain objects used directly). For Review, create DTOs if the request body differs from domain model. |

---

## M1.1 acceptance checklist

- goa-sandbox branch checked: ✅ `feature/reviews`
- working tree clean before audit: ✅ `nothing to commit, working tree clean`
- no business code changed: ✅ only `docs/goa-sandbox-review-audit.md` will be created
- src not modified: ✅
- pom.xml not modified: ✅
- docker-compose.yml not modified: ✅
- .github not modified: ✅
- audit document created: ✅ `docs/goa-sandbox-review-audit.md`
- Review implementation locations proposed: ✅
- unknowns marked as 未确认: ✅ items 1–9 documented as open questions
