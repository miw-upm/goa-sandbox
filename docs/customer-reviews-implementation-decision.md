# Customer Reviews Implementation Decision

> Decision date: 2026-05-05
> Auditor: M1.3 —封板 M1 审计结论，不含业务代码
> Basis: customer-reviews-scope.md + goa-sandbox-review-audit.md + customer-reviews-user-letter-audit.md

---

## Source documents

| Document | Role |
|---|---|
| Teacher email | Primary scope source |
| `docs/customer-reviews-scope.md` | M0 — frozen scope and architecture constraints |
| `docs/goa-sandbox-review-audit.md` | M1.1 — backend architecture, hexagonal pattern, security patterns |
| `docs/customer-reviews-user-letter-audit.md` | M1.2 — User/EngagementLetter evidence from both repos |

---

## Confirmed assignment scope

- **Backend:** goa-sandbox
- **Frontend:** goa-front
- **Review fields:** `user`, `letter`, `stars: 1..5`, `opinion: string`
- **One review per customer per Engagement Letter** — enforced at service layer
- **Customer can update their existing review** — PUT endpoint, no delete
- **Customer can only see their own Hojas de encargo / Engagement Letters** — scope rule
- **Not a public generic CRUD** — no admin management, no public listing
- **No delete** unless teacher explicitly requests it
- **goa-front:** branch `feature/reviews`, issue branches from `feature/reviews`, merge back to `feature/reviews`

---

## Architecture decision for backend

Based on M1.1 audit of goa-sandbox:

| Decision | Rationale |
|---|---|
| Follow hexagonal (ports & adapters) architecture | goa-sandbox uses `adapter/in/` + `domain/` + `adapter/out/` structure |
| Review domain model in `domain/model/Review.java` | Complaint domain model uses `@Data @Builder @NoArgsConstructor @AllArgsConstructor` |
| Review domain service in `domain/services/ReviewService.java` | Business rules, ID generation, duplicate checks — not in repository |
| Review port interface in `domain/ports/out/ReviewRepository.java` | Repository interface at domain level |
| Review entity in `adapter/out/review/ReviewEntity.java` | MongoDB `@Document`, Lombok `@Data`, `BeanUtils.copyProperties()` |
| Review persistence adapter in `adapter/out/review/ReviewPersistenceMongodb.java` | Implements `ReviewRepository`, uses Spring Data |
| Review resource in `adapter/in/resources/ReviewResource.java` | `@RestController`, `@PreAuthorize`, `@AuthenticationPrincipal Jwt` |
| API must not expose persistence entities directly | Even though Complaint returns domain object, DTO pattern is preferred per scope |
| Business rules in service layer, not in resource or repository | Matches existing ComplaintService pattern |
| Use `@Valid` + Jakarta validation on request bodies | Matches existing pattern |
| Throw `ResponseStatusException(HttpStatus.*)` for errors | Matches existing pattern |

---

## User identity decision

| Aspect | Decision | Status |
|---|---|---|
| Authenticated user source | `@AuthenticationPrincipal Jwt jwt` + `jwt.getClaimAsString("username")` | **confirmed** |
| Username claim value | Mobile string (e.g., `"600000000"`), NOT UUID | **confirmed** |
| Review.userId field type | `String` | **confirmed** |
| userId must come from JWT | Resource extracts userId from JWT, NOT from request body | **confirmed** |
| Field name in domain model | `String userId` (matches Complaint pattern) | **confirmed** |
| Exact semantic meaning | Implementation should use the same pattern as ComplaintResource. The field is called `userId` in code even though the JWT source is the mobile string. | **confirmed** |

**Implementation rule:**
```java
String userId = jwt.getClaimAsString("username"); // mobile string
// NEVER: complaint.setUserId(request.getUserId());
```

---

## Letter identity decision

| Aspect | Decision | Status |
|---|---|---|
| goa-sandbox does NOT contain EngagementLetter entity | Confirmed — no EngagementLetter class exists | **confirmed** |
| Review must not store full EngagementLetter object | Must store String identifier only | **confirmed** |
| Letter identifier field name | `String letterId` (conservative, not `barcode`) | **confirmed** |
| Exact identifier value source | `EngagementLetter.id: string` (UUID) from goa-front model | **likely, not fully confirmed** |
| Do NOT use `barcode` | Legacy Complaint barcode is not proven to be EngagementLetter identifier | **confirmed — do not use** |
| Do NOT access goa-engagement database directly | Scope rule | **confirmed** |

**M2 conservative strategy:** Name the field `letterId` in all layers. Do not assume `barcode` until later audit proves otherwise.

**M3 must resolve:** Whether `letterId` maps to `EngagementLetter.id` (UUID string) or another identifier. This affects frontend API calls to goa-engagement.

---

## Review uniqueness decision

| Aspect | Decision | Status |
|---|---|---|
| Uniqueness key | Compound: `(userId, letterId)` | **confirmed** |
| Database-level enforcement | Service-layer check: `existsByUserIdAndLetterId(userId, letterId)` before create | **confirmed** |
| Second submission | Updates existing review (upsert or check-then-update), does NOT create duplicate | **confirmed** |
| Conflict response | `HttpStatus.CONFLICT` if duplicate on create | **confirmed** |
| Unique constraint in repository | `ReviewMongoRepository.existsByUserIdAndLetterId()` | **confirmed** |

---

## Ownership validation decision

**Critical open question — HIGH severity, must resolve before M3 completion:**

goa-sandbox currently lacks EngagementLetter data. The scope says "customer can only review their own Engagement Letters." This implies ownership validation is needed, but the mechanism is **unresolved**.

Before finalizing M3, must confirm one of:

1. **Option A:** goa-sandbox has a valid API/Feign client to verify ownership — a call to goa-engagement to confirm the letter belongs to the customer. (No evidence yet.)
2. **Option B:** goa-engagement exposes an endpoint that validates customer mobile + letter id. (No evidence yet.)
3. **Option C:** Frontend only passes letters already confirmed as belonging to the current customer (token-based or JWT-authenticated fetch), and backend performs a conservative existence check only. Document as a known limitation.
4. **Option D:** Ownership validation is scoped out by teacher intent — "El usuario solo puede ver sus Hojas de encargo" refers to the frontend filtering, not the backend enforcing the relationship.

**Current conservative M3 approach:** Implement service-layer existence check: review can be created if the `letterId` is a valid format (String non-empty). Mark ownership validation as a documented limitation until confirmed.

**Do NOT:** Fake ownership validation by creating a dummy Feign client or hardcoding a lookup that always succeeds.

---

## Frontend route decision

| Aspect | Decision | Status |
|---|---|---|
| goa-front already has EngagementLetter model/service | `features/home/engagement-letter/` — fully reusable | **confirmed** |
| AuthService, HttpService, RoleGuard, ENDPOINTS | All reusable patterns | **confirmed** |
| Recommended Review frontend flow | Authenticated JWT flow (`/home/reviews`), not public token flow | **likely preferred** |
| Reason | Teacher: "El usuario solo puede ver sus Hojas de encargo." Review is tied to authenticated customer identity, not a public link. | — |
| Existing customer token routes | `/customer/read-engagement-letter/:urlId/:token` — public, token-based | **confirmed** |
| Current home routes for EngagementLetters | Requires ADMIN/MANAGER/OPERATOR only — CUSTOMER excluded | **confirmed** |
| Option: public token flow for Review | Would reuse `/customer/` pattern. Possible but needs teacher confirmation. | **unresolved** |
| Option: authenticated CUSTOMER route | Would require adding a new route under home with `RoleGuard` + `[Role.CUSTOMER]` | **needs decision** |

**Unresolved — do not implement until decision is made in M5/M7:**
- Exact route path: `/home/reviews` vs `/customer/reviews` vs integrated into existing engagement-letter screen
- This decision affects whether Review is shown inside the home dashboard or as a standalone customer-facing page

---

## API path decision

| Aspect | Decision | Status |
|---|---|---|
| Gateway path prefix | Likely `http://localhost:8080/api/goa-sandbox` | **not confirmed** |
| Review API path | `/reviews` (backend `@RequestMapping`) | **likely** |
| Full gateway path | `http://localhost:8080/api/goa-sandbox/reviews` | **likely, not confirmed** |
| goa-sandbox endpoint constant in goa-front | NOT yet defined — must add `REST_SANDBOX` to `environment.ts` | **deferred to M5** |
| Do NOT hardcode URLs in components | Use `ENDPOINTS` pattern | **confirmed** |
| Confirm gateway route before M5/M7 | Must verify with docker-compose or teacher documentation | **deferred** |

---

## M2 readiness decision

### Allowed to start M2 with safe parts

| Component | Scope |
|---|---|
| `domain/model/Review.java` | Domain model: `String id`, `String userId`, `String letterId`, `int stars`, `String opinion`, `LocalDateTime createdAt`, `LocalDateTime updatedAt` |
| `adapter/out/review/ReviewEntity.java` | MongoDB entity with `@Document(collection = "reviews")`, Lombok, BeanUtils mapping |
| `adapter/out/review/ReviewMongoRepository.java` | Spring Data `MongoRepository<ReviewEntity, String>` with `existsByUserIdAndLetterId()` |
| `adapter/out/review/ReviewPersistenceMongodb.java` | Persistence adapter implementing `ReviewRepository` |
| `domain/ports/out/ReviewRepository.java` | Domain port interface: `create()`, `read()`, `update()`, `existsByUserIdAndLetterId()` |
| `domain/services/ReviewService.java` | Business logic: stars 1..5 validation, uniqueness check, userId from parameter, letterId from parameter |
| DTOs | `ReviewCreateDto`, `ReviewUpdateDto` with `@Min(1) @Max(5)`, `@NotBlank` |
| `adapter/in/resources/ReviewResource.java` | REST endpoints: `POST /reviews`, `GET /reviews/{id}`, `PUT /reviews/{id}` with security annotations |
| Unit tests | `ReviewServiceTest.java` — test stars validation, uniqueness, update logic |
| Integration tests | `ReviewResourceIT.java` — test JWT auth, CRUD, conflict |

### Not allowed yet / must defer

| Component | Reason |
|---|---|
| Final ownership validation against EngagementLetter | Mechanism not confirmed — HIGH open question |
| Feign client to goa-engagement | Not found, may not be needed |
| Frontend Review component | M5 scope — needs route decision first |
| Frontend `REST_SANDBOX` constant | M5 scope — needs gateway confirmation |
| Frontend star rating UI | M6 scope |
| Integration with goa-engagement API | M7 scope — needs confirmation of EngagementLetter identifier |
| Final gateway integration test | M7 scope |

---

## Risks carried into M2/M3

| Risk | Severity | Mitigation |
|---|---|---|
| Wrong letter identifier | HIGH | Conservative naming `letterId` in all layers; defer exact mapping to M3 |
| Fake ownership validation | HIGH | Document ownership validation as unresolved until confirmed |
| Public token flow vs authenticated flow | MEDIUM | Make route decision in M5 before implementing frontend |
| Gateway path mismatch | MEDIUM | Confirm before M7 integration; use environment-based URLs |
| Returning entity instead of DTO | LOW | Create DTOs even if current Complaint returns domain object; scope prefers DTOs |
| Exposing generic CRUD accidentally | LOW | Guard all endpoints with `@PreAuthorize`; no public `GET /reviews` listing |

---

## Final implementation guardrails

These rules must never be violated in M2–M8:

| Rule | Reason |
|---|---|
| No `GET /reviews` returning all reviews | Not a public listing — customers only see their own |
| No `DELETE /reviews` unless teacher requests | No delete per scope document |
| No admin review management unless teacher requests | Scope explicitly excludes admin moderation |
| No cross-database access to goa-engagement | Scope rule: "do not access another microservice database directly" |
| Never trust userId from request body | Must extract from JWT; request body may contain letterId and stars/opinion only |
| No duplicate review per user + letter | Second submission updates existing review; throw CONFLICT on duplicate create |
| Stars must be validated backend-side | Even if frontend has star UI, backend must enforce 1..5 |
| No hardcoded API URLs in components | Use ENDPOINTS pattern |
| No credentials or tokens in documentation | Scope sensitive credentials rule |

---

## M1 acceptance — full milestone closure

| Milestone | Document | Commit |
|---|---|---|
| M0.1 | `customer-reviews-scope.md` | `3ace54e` |
| M0.2 | goa-front branch `feature/reviews` created | (local only) |
| M0.3 | goa-sandbox branch `feature/reviews` created | (local only) |
| M0.4 | scope document committed | `3ace54e` |
| M0.5 | M0 acceptance — all checklist items passed | — |
| M1.1 | `goa-sandbox-review-audit.md` | `4b6ea9d` |
| M1.2 | `customer-reviews-user-letter-audit.md` | `01f3c01` |
| M1.3 | `customer-reviews-implementation-decision.md` | (pending) |

---

## M1.3 acceptance checklist

- M1.1 and M1.2 docs read: ✅ all three source documents reviewed
- no src modified: ✅
- no pom.xml modified: ✅
- no package.json modified: ✅
- no docker-compose.yml modified: ✅
- no .github modified: ✅
- implementation decision doc created: ✅ `docs/customer-reviews-implementation-decision.md`
- M2 safe scope defined: ✅ (domain model, entity, repository, persistence, service, resource, DTOs, tests)
- M3 unresolved ownership questions preserved: ✅ (HIGH open question documented)
- no push performed: ✅
